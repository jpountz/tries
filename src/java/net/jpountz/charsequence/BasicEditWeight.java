package net.jpountz.charsequence;

public interface BasicEditWeight extends EditWeight {

	double insertionCost();
	double deletionCost();
	double substitutionCost();
	double transpositionCost();

}
