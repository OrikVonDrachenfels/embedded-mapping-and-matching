# PRISMA - Privacy-Preserving Schema Matcher

This Project was implemented using the [Schematch](https://github.com/avielhauer/schematch) repository.
Please check [the Wiki](https://github.com/avielhauer/schematch/wiki) for further information on the repository.

## Setup
This project requires Java JDK 17 and Maven >=3.9.2. We suggest developing and running the project in IntelliJ IDEA.
For setup, IntelliJ should guide you to install the correct Java JDK and download the Maven dependencies for you.
In case of problems with Maven dependencies, try to [reload the project](https://www.jetbrains.com/help/idea/delegate-build-and-run-actions-to-maven.html#maven_reimport).

You can run the project out of the box, as it comes with data and default configurations.
For a successful run, you should see this log line at the end of your console:
```
[INFO ] <timestamp> [main] de.uni_marburg.schematch.Main - Ending Schematch
```
(taken from [wiki](https://github.com/avielhauer/schematch/wiki#setup))

To install and use PRISMA, LEAPME, and EmbDI we provide a [Docker Compose](https://docs.docker.com/compose/) file.
Build the corresponding images by running:

```
sudo docker compose --verbose -f sota.docker-compose.yml build
```

Additionally, a Pyro server needs to be started. 
The files can be found in `src/main/resources/pyro`.
Download the required `metanome-cli-1.1.0.jar` and `pyro-distro-1.0-SNAPSHOT-distro.jar` from [here](https://github.com/sekruse/metanome-cli/releases/tag/v1.1.0) 
and [here](https://github.com/HPI-Information-Systems/pyro/releases/tag/v1.0) and place it in the pyro directory.
Install the required python dependencies using [poetry](https://python-poetry.org/). 

## Running PRISMA

To run PRISMA, LEAPME, and EmbDI execute:
```
sudo docker compose --verbose -f sota.docker-compose.yml up
```
Start the Pyro server, which is required for Data Profiling using
```python pyro_server.py```.


To specify the to be used matchers and their settings, check out the `first_line_matchers.yaml`.
The to be used datasets can be selected in `datasets.yaml`.

Finally, running Schematch's Main function (e.g., using IntelliJ IDEA) will execute all defined
scenarios.

The run's result can be found in the `/results` folder.