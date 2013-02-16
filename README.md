SPLlift
=====================
SPLlift is an approach that lets you automatically reuse inter-procedural static data-flow analyses implemented in the IFDS framework also for entire Software Product Lines (SPLs). If, for normal programs, the analysis reports that a piece of analysis information may or may not hold at a given statement, the SPL-version reports a Boolean constraint telling you under which feature combinations this fact could hold at that statement. This all can be done without having to change a single line of code on the side of the client analysis.

Who are the developers of SPLlift?
----------------------------------
SPLlift is a joint development of Eric Bodden Claus Brabrand, Tarsis Toledo, Marcio Ribeiro, Paulo Borba and Mira Mezini. It is currently maintained by [Eric Bodden][2].

Why is SPLlift called SPLlift?
------------------------------
It is called SPLlift because it allows existing analyses that are designed for "normal Java programs" to be lifted to analyses that operate on entire software product lines.

Are there any publications on SPLlift?
--------------------------------------
Yes, there are:

[Transparent and Efficient Reuse of IFDS-based Static Program Analyses for Software Product Lines][4] (Eric Bodden, Társis Tolêdo, Márcio Ribeiro, Claus Brabrand, Paulo Borba, Mira Mezini), Technical report TUD-CS-2012-0239, EC SPRIDE, Technische Universität Darmstadt, 2012.

What is IFDS/IDE?
-----------------
[IFDS][1] is a general framework for solving inter-procedural, finite, distributive subset problems in a flow-sensitive, fully context-sensitive manner. From a user's perspective, IFDS allows static program analysis in a template-driven manner. Users simply define flow functions for an analysis problem but don't need to worry about solving the analysis problem. The latter is automatically taken care of by the solver, in this case by [Heros][3].
[IDE][2] is an extension of IFDS that allows more expressive computations. Heros implements an IDE solver and supports IFDS problems as special cases of IDE. SPLlift replaces this "adapter" by a more expressive one that automatically tracks feature constraints.

How can I contribute to SPLlift?
--------------------------------
Contributions are more than welcome! It is easiest to fork the project on Github. Then make your modifications on the fork and send us a pull request. This will allow us easy re-integration.

[1]: http://dx.doi.org/10.1145/199448.199462
[2]: http://dx.doi.org/10.1016/0304-3975(96)00072-2
[3]: https://github.com/Sable/heros/
[4]: http://www.bodden.de/pubs/btr+12transparent.pdf