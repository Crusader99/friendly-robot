# friendly-robot

A tool that helps you checking LaTeX spell and grammar mistakes.

* Spell &amp; grammar checking is based on [LanguageTool](https://github.com/languagetool-org/)
* No webservice invoked. LaTeX parsing and spell checking happens local
* Java 8 Runtime recommended but should work on other versions, too

---

### How this works:
1. Parses plain text from `*.tex` source-files
2. Performs spell &amp; grammar checking
3. Prints potential errors with line number from latex-source-file
4. Suggests corrections to most errors

--- 

### How to use:
1. Build source or download binaries
2. Extract content of .zip file and go to `bin`-folder
3. Run tool with `./friendlyrobot help` for help

##### Example
Basic example command for spell &amp; grammar checking:
`./friendlyrobot check document.tex en-US`

##### Supported languages:
```
en, en-US, en-GB, en-AU, en-CA, en-NZ, en-ZA, fa, fr, de, de-DE, de-AT, de-CH, pl-PL, ca-ES, it, br-FR, nl, pt, pt-PT, pt-BR, pt-AO, pt-MZ, ru-RU, ast-ES, be-BY, zh-CN, da-DK, eo, gl-ES, el-GR, ja-JP, km-KH, ro-RO, sk-SK, sl-SI, es, sv, ta-IN, tl-PH, uk-UA
```


---

### Build source:

Run `./gradlew clean build` on Unix or `gradlew.bat clean build` on Windows.
The compiled *.jar-files can be found in `build/distributions/FriendlyRobot-x.x.x.zip`.