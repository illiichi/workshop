## requirements
- Java SDK (Version 8)

- Leiningen (https://leiningen.org/)

- editor
It is highly recommended to use a text editor or an IDE .

- jack (linux user only)
see: https://overtone.github.io/docs.html#_dependencies

## to make sure that sound could play
1. start leiningen with `project.clj`
2. open `src/workshop/core.clj`
3. evaluate `(use 'overtone.live)`
4. evaluate `(demo 3 (sin-osc 440))`
5. now you will hear the beep sound

## License

MIT
