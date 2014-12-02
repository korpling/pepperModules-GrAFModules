![SaltNPepper project](./md/img/SaltNPepper_logo2010.png)
# pepperModules-GrAFModules
This project provides a plugin for the linguistic converter framework Pepper (see https://github.com/korpling/pepper). Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. That means converting data from a format _A_ to format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle a number of n formats.

![n:n mappings via SaltNPepper](./md/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter.

This project provides an importer to import data coming the GrAF format (see http://www.americannationalcorpus.org/graf-wiki) to Salt. A detailed description of that mapping can be found in section [GrAFImporter](#details).

## Requirements
Since the here provided module is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

### Windows

    pepperStart.bat is https://korpling.german.hu-berlin.de/saltnpepper/repository/repo/de/hu_berlin/german/korpling/saltnpepper/pepperModules/pepperModules-GenericXMLModules/1.1.2/de.hu_berlin.german.korpling.saltnpepper.pepperModules.pepperModules-GenericXMLModules_1.1.2.zip

### Linux/Unix

    bash pepperStart.sh is https://korpling.german.hu-berlin.de/saltnpepper/repository/repo/de/hu_berlin/german/korpling/saltnpepper/pepperModules/pepperModules-GenericXMLModules/1.1.2/de.hu_berlin.german.korpling.saltnpepper.pepperModules.pepperModules-GenericXMLModules_1.1.2.zip


## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter>. The GenericXMLImporter is an importer module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://github.com/korpling/pepper). 

### a) Identify the module by name

```xml
<importer name="GrAFImporter" path="PATH_TO_CORPUS"/>
```

### b) Identify the module by formats
```xml
<importer formatName="xml" formatVersion="1.0" path="PATH_TO_CORPUS"/>
```

### c) Use properties
```xml
<importer name="GrAFImporter" path="PATH_TO_CORPUS">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</key>
</importer>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you even think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project has been funded by the [department of corpus linguistics and morphology](https://www.linguistik.hu-berlin.de/institut/professuren/korpuslinguistik/) of Humboldt-Universität zu Berlin, [Georgetown University](http://www.georgetown.edu/), [KOMeT](http://korpling.german.hu-berlin.de/komet/) and the [Sonderforschungsbereich 632](https://www.sfb632.uni-potsdam.de/en/). 

## License
  Copyright 2014 Humboldt-Universität zu Berlin.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.



# GrAFImporter

The TEIImporter imports data coming from [GrAF](http://www.americannationalcorpus.org/graf-wiki) files to a [Salt](https://github.com/korpling/salt) model. This importer provides a wide range of customization possibilities via the here described set of properties. 

## Properties

| Name of property                              | Type of property | optional/mandatory | default value      |
|-----------------------------------------------|------------------|--------------------|--------------------|
| [graf.importer.syntaxLayer](#syn)         | String          | optional           | f.ptb              |
| [graf.importer.tokenizationLayer](#tok)         | String           | optional           | f.seg             |
| [graf.importer.posLayer](#pos)              | String          | optional           | f.penn             |
| [graf.importer.headerEnding](#hdr)             | String          | optional           | .hdr               |

<a name="syn"></a>
### graf.importer.syntaxLayer

This property determines the name for the syntax layer in the GrAF encoded corpus.

<a name="tok"></a>
### graf.importer.tokenizationLayer

This property determines the name for the tokenization layer in the GrAF encoded corpus.

<a name="pos"></a>
### graf.importer.posLayer

This property determines the name for the pos annotations in the GrAF encoded corpus.

<a name="hdr"></a>
### graf.importer.headerEnding

This property determines ending of the header files.

