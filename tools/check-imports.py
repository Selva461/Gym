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
Math System Thread Runnable Comparator Number Enum Companion Class UUID File'''.split())

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
    for line in code.split('\n'):
        if line.startswith('import '):
            imports.add(line.split('.')[-1].strip())
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
    if missing:
        bad += 1
        print(f"{p.replace(ROOT+'/','')}")
        for m in missing:
            where = [k for k, v in decls.items() if m in v]
            hint = f"  -> import {where[0]}.{m}" if where else "  (not declared in this project)"
            print(f"    {m}{hint}")
print(f"\n{bad} file(s) with unresolvable references" if bad else "\nno unresolvable references")
