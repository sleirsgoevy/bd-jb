use64

global _start
extern main

section .text.startup

_start:
jmp main
