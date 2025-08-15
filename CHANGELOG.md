# Simpler Wooden Pipes

## [1.8]
- rewrote the pipe logic, no more random fluid transfer
- improved pump logic

## [1.7]
- fix server crash
- improved pipe model renderer

## [1.6]
- fix crash with `toolprogression` mod
- fix weird creative inventory behavior

## [1.5]
- allow changing the default pipe material
- fixed a edge case where the default pipe wouldn't have any nbt data and would connect to all other pipes
- fixed a edge case where the default pipe wouldn't be able to transport fluids at all
- break particles will now use the pipe's material texture
- mining particles will now use the pipe's material texture
- running particles will now use the pipe's material texture
- landing particles will now use the pipe's material texture
- overhauled extraction item config handler

## [1.4]
- hide default pipes from JEI, when recipes are disabled
- improve sound handling when breaking pipes
- improve pipe naming depending on the block used as material
- improve tooltip coloring

## [1.3]
- Initial fork
- improved melting mechanic
- improved config