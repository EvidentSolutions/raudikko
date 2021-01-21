# Raudikko

Raudikko is Java library for performing morphological analysis on Finnish language. Raudikko uses
[Voikko](https://voikko.puimula.org)'s morphology files and is based on Voikko, but unlike
[Voikko's Java-interface](https://voikko.puimula.org/java.html), it is implemented purely in Java and needs no native
libraries. Raudikko is also quite a bit faster than Voikko's Java-interface.

## Usage

Add dependency to your build file:

```kotlin
implementation("fi.evident.raudikko:raudikko:0.1.1")
```

Create an analyzer and use it to produce `Analysis`-objects for given words:

```java
// First load and compile the morphology. This is a relatively expensive operation 
// that should be done only once. The loaded morphology is immutable and can be shared.
Morphology morphology = Morphology.loadBundled();

// Create an analyzer from the morphology. Creating an analyzer is a cheap operation. 
// Analyzers have some cached state and can't be shared between different threads. 
Analyzer analyzer = morphology.newAnalyzer();

// Analyze some words
System.out.println(analyzer.analyze("kissoittansa"));
System.out.println(analyzer.analyze("kahdellakymmenelläseitsemällä"));
```

## Compatibility with Voikko

Raudikko is carefully tested against Voikko, making sure that the output of Raudikko and Voikko agree for all inputs.
The below table lists the version of Voikko that Raudikko is tests against.

| Raudikko version       | Voikko version           | Morphology  |
| ------------- |:-------------:| -----:|
| 0.1.1    | 4.3 | [9f0d3d](https://github.com/voikko/corevoikko/commit/9f0d3de39ac23a9776d1ec8c30a157a707955a50) |

## Acknowledgements

Most of Raudikko's code is based heavily on Voikko and the used morphology files come directly from Voikko. None
of this would be possible without the great work of [Harri Pitkänen](https://github.com/hatapitk) and other 
contributors to Voikko.

## Copyright and license information

Raudikko is available under [GNU General Public License version 3](https://opensource.org/licenses/GPL-3.0),
or (at your option) any later version.  Apart from the morphology in directory `resources/morpho`, all the 
content in this repository is also licensed under [Mozilla Public License 2.0](https://opensource.org/licenses/MPL-2.0),
which you may choose to use instead.

Raudikko is a port of Voikko, and therefore Voikko's original copyright holders hold copyrights on parts of Raudikko.
See Voikko's [LICENSE](https://github.com/voikko/corevoikko/blob/master/LICENSE) and
[CONTRIBUTORS](https://github.com/voikko/corevoikko/blob/master/voikko-fi/CONTRIBUTORS) for details.

Apart from the original copyright holders of Voikko, Evident Solutions Oy holds copyright on Raudikko. 
