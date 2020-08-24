#include <iostream>
#include "fcpp.hh"

int quadrat(int x)
{
	return x*x;
}

int potenz(int x, int exp) 
{
	return cond(exp==1,x,(cond(exp%2==0,potenz(quadrat(x),exp/2),potenz(x*potenz(x,exp-1),1))));

	/*
	Wenn exp==1 dann gib x aus
	Ansonsten
		Wenn exp%2==0
			Rechne die Potenz von (x²)^exp/2
		sonst   Rechne die Potenz von x*x^exp-1 
	*/
}
int main()
{
	int x;
	int exp;
	print("Geben sie x ein:");	
	std::cin>>x;
	print("Geben sei exp ein:");
	std::cin>>exp;
	print(potenz(x,exp));
}
