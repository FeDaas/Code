#include <iostream>
#include "fcpp.hh"

int addZ(int Zahl, int Teiler, int add)
{
	return cond(Teiler>Zahl/2,add,cond(Zahl%Teiler==0,addZ(Zahl,Teiler+1,add+Teiler),addZ(Zahl,Teiler+1,add)));
	
	/* Wenn Teiler>Zahl/2 gib add aus.
		Ansonsten prüfe Zahl%Teiler=0
			Ja:add=add+Teiler
			Nein:add=add	
		Teiler++			*/
}

bool vollkommen(int Zahl)
{
	return cond(Zahl==addZ(Zahl,1,0),true, false);
}

int main()
{
	int Zahl;	
	print("Geben sie eine Zahl ein:");	
	std::cin>> Zahl;	
	print(vollkommen(Zahl));
}
