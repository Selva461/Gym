#!/usr/bin/env python3
"""
Catches the one class of Kotlin error this sandbox cannot: a capitalised reference
with no import and no declaration in the same package.

Not a type checker. It knows nothing about signatures or generics. It answers one
question — "could the compiler resolve this name at all" — which is exactly the
mistake that costs a CI round trip, and the only one worth a script.
"""
import re, sys, os, collections

ROOT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'app/src/main/java/dev/avles/gym')
KOTLIN = set('''String Long Boolean Int Unit List Map Double Float Set Any Nothing Pair Triple
Array Char Byte Short Exception Throwable RuntimeException IllegalStateException
IllegalArgumentException Comparable Iterable Sequence Result Regex Volatile Synchronized
JvmStatic JvmField Deprecated Suppress OptIn Target AnnotationTarget Retention
ArrayDeque LinkedHashMap HashMap HashSet MutableList MutableMap MutableSet
Math System Thread Runnable Comparator Number Enum Companion Class UUID File
StringBuilder CharSequence'''.split())

# Modifier extensions this project uses, and where each one lives. Every one is a
# top-level function needing its own import — `Modifier.clickable {}` fails with
# "Unresolved reference: clickable" and nothing else.
MODIFIER_EXTENSIONS = {
    'clickable': 'androidx.compose.foundation',
    'background': 'androidx.compose.foundation',
    'border': 'androidx.compose.foundation',
    'verticalScroll': 'androidx.compose.foundation',
    'horizontalScroll': 'androidx.compose.foundation',
    'padding': 'androidx.compose.foundation.layout',
    'fillMaxWidth': 'androidx.compose.foundation.layout',
    'fillMaxHeight': 'androidx.compose.foundation.layout',
    'fillMaxSize': 'androidx.compose.foundation.layout',
    'height': 'androidx.compose.foundation.layout',
    'width': 'androidx.compose.foundation.layout',
    'size': 'androidx.compose.foundation.layout',
    'heightIn': 'androidx.compose.foundation.layout',
    'widthIn': 'androidx.compose.foundation.layout',
    'aspectRatio': 'androidx.compose.foundation.layout',
    'offset': 'androidx.compose.foundation.layout',
    'clip': 'androidx.compose.ui.draw',
    'alpha': 'androidx.compose.ui.draw',
    'semantics': 'androidx.compose.ui.semantics',
}

# Everything the project declares, and which package it lives in.
decls = collections.defaultdict(set)
files = []
for dirpath, _, names in os.walk(ROOT):
    for n in names:
        if n.endswith('.kt'):
            p = os.path.join(dirpath, n)
            files.append(p)
            src = open(p).read()
            pkg = re.search(r'^package\s+(\S+)', src, re.M)
            pkg = pkg.group(1) if pkg else ''
            for m in re.finditer(
                r'^\s*(?:@\w+\s+)*(?:public |internal |private |abstract |sealed |open |data |value |enum |annotation )*'
                r'(?:class|interface|object|enum class)\s+([A-Z][A-Za-z0-9_]*)', src, re.M):
                decls[pkg].add(m.group(1))
            for m in re.finditer(r'^\s*(?:@Composable\s+)?(?:internal |private |public )?fun\s+([A-Z][A-Za-z0-9_]*)\s*\(', src, re.M):
                decls[pkg].add(m.group(1))
            for m in re.finditer(r'^\s*(?:internal |private |public )?(?:const )?val\s+([A-Z][A-Za-z0-9_]*)\s*[:=]', src, re.M):
                decls[pkg].add(m.group(1))

targets = sys.argv[1:] or files
bad = 0
for p in sorted(targets):
    src = open(p).read()
    pkg = re.search(r'^package\s+(\S+)', src, re.M)
    pkg = pkg.group(1) if pkg else ''
    code = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    code = re.sub(r'//[^\n]*', '', code)
    interp = ' '.join(re.findall(r'\$\{([^}]*)\}', code))
    stripped = re.sub(r'"(?:[^"\\]|\\.)*"', '""', code)

    imports = set()
    seen_lines = set()
    duplicate_imports = []
    by_simple_name = {}
    for line in code.split('\n'):
        if not line.startswith('import '):
            continue
        simple = line.split('.')[-1].strip()
        imports.add(simple)
        # Kotlin rejects the same simple name imported from two places, and an
        # exact repeat as "conflicting import ... is ambiguous" — which reads like
        # a name clash rather than the copy-paste it usually is.
        if line in seen_lines:
            duplicate_imports.append(f'{simple}  -> imported twice: {line.strip()}')
        elif simple in by_simple_name and by_simple_name[simple] != line:
            duplicate_imports.append(
                f'{simple}  -> two imports share this name: '
                f'{by_simple_name[simple].strip()} and {line.strip()}'
            )
        seen_lines.add(line)
        by_simple_name[simple] = line
    # Fully-qualified uses need no import.
    fq = set(re.findall(r'\b(?:[a-z][a-z0-9_]*\.){2,}([A-Z][A-Za-z0-9_]*)', code))

    body = '\n'.join(l for l in stripped.split('\n')
                     if not l.startswith(('import ', 'package '))) + '\n' + interp
    used = set(re.findall(r'(?<![.\w])([A-Z][A-Za-z0-9_]*)\b', body))
    # Names declared in this same file resolve without an import.
    here = set(re.findall(r'(?:class|interface|object|fun|enum class|val|const val)\s+([A-Z][A-Za-z0-9_]*)', code))
    # Enum entries, wherever they sit on the line: "enum class View { FRONT, BACK }"
    # declares both, and neither starts its own line.
    for blk in re.findall(r'enum class\s+\w+[^{]*\{(.*?)\n\s*(?:\}|;)', code, re.S):
        here |= set(re.findall(r'\b([A-Z][A-Z0-9_]*)\b', blk))
    here |= set(re.findall(r'^\s*([A-Z][A-Z0-9_]{2,})\s*[,(]', code, re.M))
    here |= set(re.findall(r'<([A-Z])[,>]', code))                              # type params

    missing = sorted(used - imports - fq - here - KOTLIN - decls[pkg])

    # Lowercase references need their own rules: the capitalised-name scan above
    # cannot see them, and they are exactly what goes missing when code moves
    # between files. Both of these cost a CI round trip before they were added.

    # `val x by remember {}` compiles only with the getValue operator imported, and
    # `var x by` needs setValue as well. The error it produces names a delegate
    # rather than an import, which is not where anybody looks first.
    if re.search(r'\bval\s+\w+\s+by\s', stripped) and 'getValue' not in imports:
        missing.append('getValue  -> import androidx.compose.runtime.getValue (for `val x by ...`)')
    if re.search(r'\bvar\s+\w+\s+by\s', stripped) and 'setValue' not in imports:
        missing.append('setValue  -> import androidx.compose.runtime.setValue (for `var x by ...`)')

    # Modifier extensions are top-level functions, so each one is its own import.
    for fn, path in MODIFIER_EXTENSIONS.items():
        if re.search(r'\.' + fn + r'\s*[({]', stripped) and fn not in imports:
            missing.append(f'{fn}  -> import {path}.{fn}')
    missing.extend(duplicate_imports)
    missing = sorted(set(missing))
    if missing:
        bad += 1
        print(f"{p.replace(ROOT+'/','')}")
        for m in missing:
            if ' -> ' in m:          # the rule already wrote its own suggestion
                print(f"    {m}")
                continue
            where = [k for k, v in decls.items() if m in v]
            hint = f"  -> import {where[0]}.{m}" if where else "  (not declared in this project)"
            print(f"    {m}{hint}")
print(f"\n{bad} file(s) with unresolvable references" if bad else "\nno unresolvable references")
