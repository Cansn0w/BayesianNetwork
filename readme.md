# Inference on Bayesian network

Inference with Variable Elimination and Markov Chain Monte Carlo algorithms on Bayesian network.

## Authors:
 * [Di Lu](https://github.com/namoshizun)
 * [Chenrui Liu](https://github.com/Cansn0w)
 
## Introduction

This is a Java implementation of the Bayesian Network and its inference methods Variable Elimination (VE) and Markov Chain Monte Carlo (MCMC). This program supports querying the network with different algorithms and/or benchmark them.

This project was initially developed for the subject "COMP3608 Intro to Artificial Intelligence" at the University of Sydney. The program was required to support inference on the following specific cancer network, however, our implementation support the construction and inference on general Bayesian networks, and a network builder is also provided to simplify the process of building the network.

The assignment specific network is provided below as well as implemented in the program.

```
P(m) = 0.20

P(i|m) = 0.80 
P(i|¬m) = 0.20

P(b|m) = 0.20
P(b|¬m) = 0.05

P(c|i,b) = 0.80 
P(c|¬i,b) = 0.80 
P(c|i,¬b) = 0.80
P(c|¬i,¬b) = 0.05

P(s|b) = 0.80 
P(s|¬b) = 0.60
```

## Requirement

JDK version 1.7 or above

## Usage

To use the program, you will first need to compile the source code with javac.

### Query the network

Execute the compiled program with java and provide necessary informations

The first line must be either "VE" or "MCMC" to select an algorithm.
The second line must be the number of queries.
Then, queries should be provided one line each in the format "P(ask|evidences,)".
The program will then output computed probabilities.

A example usage on VE:

```
$ java Main
VE
2
P(c|m,b)
P(-c|m,-b)
```

Output

```
0.800000
0.650000
```

A example usage on MCMC:

```
$ java Main
MCMC 1000
1
P(c|m)
```

Output

```
0.479000
```


### Benchmarking different algorithms

Command line arguments can be given to start the benchmark mode in the format

```
java Main QUERY METHOD #ITERATION [#SAMPLES(if you are testing MCMC)]
```

for example:

```
$ java Main P(c|m,b) MCMC 100 1000
```

Example output

```
MCMC computing P(c|m,b) with 100 iterations using sample size 1000.
SOME MILLESECONDS
```


## Extending the Bayesian Network:

The Bayesian Network is implemented with a builder to simplify the process of building a network, in which client code can create a network by only providing network specification without worrying about connecting all network nodes.

An example is provided in Main.java file as required by the assignment.

To add another node 'G' whose parent node is S and has a probability distribution like

```
P(g|s) = 0.3
P(g|-s) = 0.2
```

you may open Main.java and add the folloing in getNetwork() method:

		net.addNode("G", new String [] {"T", "F"}, new String[] {"S"}, new String [] {
				"G = T, S = T: 0.3",
				"G = T, S = F: 0.2",
				"G = F, S = T: 0.7",
				"G = F, S = F: 0.8",
				});

For more implementation detail, please refer to source file comments.
