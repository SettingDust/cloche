"""Run with Python 3.9+, JDK 17 (JAVA_HOME), Gradle and two plugin-under-test metadata files."""
import argparse
import hashlib
import re
import os
from pathlib import Path
import shutil
import subprocess
import tempfile


parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('baseline', type=Path)
parser.add_argument('candidate', type=Path)
parser.add_argument('--gradle', default='gradle')
parser.add_argument('--offline', action='store_true')
parser.add_argument('--output', type=Path, required=True, help='Parent directory for a fresh fixture and logs')
args = parser.parse_args()
args.output.mkdir(parents=True, exist_ok=True)
work = Path(tempfile.mkdtemp(prefix='game-jars-', dir=args.output)).resolve()
print(f'Fixture and evidence: {work}', flush=True)
for name in ('build.gradle.kts', 'acceptance.gradle.kts', 'settings.gradle.kts', 'gradle.properties',
             'MANIFEST.MF', 'GameJarProbe.java'):
    shutil.copy(Path(__file__).with_name(name), work)
(work / 'run').mkdir()
jdk = Path(os.environ['JAVA_HOME']) / 'bin'
subprocess.run([jdk / 'javac', 'GameJarProbe.java'], cwd=work, check=True)
subprocess.run([jdk / 'jar', 'cfm', 'probe.jar', 'MANIFEST.MF', 'GameJarProbe.class'], cwd=work, check=True)


def run(task, label, baseline=False):
    log = work / f'{label}.log'
    metadata = (args.baseline if baseline else args.candidate).resolve()
    flags = (['-Pbaseline'] if baseline else []) + (['--offline'] if args.offline else [])
    with log.open('w') as stream:
        subprocess.run([args.gradle, '--console=plain', f'-PclocheClasspathMetadata={metadata}', *flags, task],
                       cwd=work, stdout=stream, stderr=subprocess.STDOUT, check=True)
    return log.read_text(errors='replace').splitlines()


def observation(lines):
    result = [line for line in lines if line.startswith(('GAME_JARS_ARG ', 'GAME_JARS_CP ', 'GAME_JARS_PATH '))]
    assert any(line.startswith('GAME_JARS_ARG ') for line in result)
    assert any(line.startswith('GAME_JARS_CP ') for line in result)
    assert 'GAME_JARS_PATH null' in result
    # Gradle keys transform storage by the plugin implementation classpath. Compare the
    # transformed path suffix and bytes, while preserving entry order and every other path.
    for i, line in enumerate(result):
        if line.startswith('GAME_JARS_CP '):
            entries = []
            for entry in line[len('GAME_JARS_CP '):].split(os.pathsep):
                path = Path(entry)
                match = re.fullmatch(r'(.*/caches/[^/]+/transforms/)[0-9a-f]+(/transformed/.+)', path.as_posix())
                if match and path.is_file():
                    entries.append((match.groups(), hashlib.sha256(path.read_bytes()).hexdigest()))
                else:
                    entries.append(entry)
            result[i] = entries
    return result


baseline = observation(run('runServer', 'baseline', baseline=True))
run('recordPatchedPath', 'producer-path')
# The game-test run reads the compilation's mod-id artifact during dependency resolution.
run('writeModId', 'test-mod-id')
patched = Path((work / 'expected-patched.txt').read_text()).resolve()
assert patched.parent == work / 'build' / 'minecraft' and patched.suffix == '.jar', patched
for task, marker in [('consumeGameJar', 'GAME_JARS_CONSUMED'), ('runTest', 'GAME_JARS_VERIFIED')]:
    patched.unlink()
    assert not patched.exists()
    (work / f'{task}-before.txt').write_text(f'absent: {patched}\n')
    lines = run(task, task)
    producer = lines.index('> Task :resolvePatchedMinecraft')
    opened = lines.index(f'{marker} {patched}')
    assert producer < opened and patched.is_file()
assert observation(run('runServer', 'ordinary')) == baseline, 'Baseline JVM arguments or classpath changed'
print('PASS: both cold reads, selected JVM file identity and declarations, ordinary JVM args and classpath')
