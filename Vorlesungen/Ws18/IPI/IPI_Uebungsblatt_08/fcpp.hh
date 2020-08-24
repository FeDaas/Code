#pragma once

#include<iostream>
#include<string>
#include<math.h>
#include<stdio.h>
#include<stdlib.h>

// Die cond Funktion ist ein Makro
#define cond( a, b, c ) ( (a) ? (b) : (c) )

// Drucke das Argument und gebe es zurueck
template<typename T>
T print ( T x ) 
{
  std::cout.precision( 16 );
  std::cout << x << std::endl;
  return x;
}

// Drucke Argument 1 und gebe das letzte Argument zurueck
template<typename T1, typename T2>
T2 print ( T1 x1, T2 x2 ) 
{
  std::cout.precision( 16 );
  std::cout << x1 << std::endl;
  return x2;
}

// Drucke Argumente 1-2 und gebe das letzte Argument zurueck
template<typename T1, typename T2, typename T3>
T3 print ( T1 x1, T2 x2, T3 x3 ) 
{
  std::cout.precision( 16 );
  std::cout << x1 << " " << x2 << std::endl;
  return x3;
}

// Drucke Argumente 1-3 und gebe das letzte Argument zurueck
template<typename T1, typename T2, typename T3, typename T4>
T4 print ( T1 x1, T2 x2, T3 x3, T4 x4 ) 
{
  std::cout.precision( 16 );
  std::cout << x1 << " " << x2 << " " << x3 << std::endl;
  return x4;
}

// gebe einfach Null zurueck. Vorsicht: Koennte bei Optimierung dazu 
// fuehren, dass das Argument gar nicht berechnet wird.
template<typename T>
int dump ( T x )
{
  return 0;
}

// Gebe eine int Zahl ein und liefere diese als Ergebnis
int enter_int ()
{
  int x;
  std::cin >> x;
  return x;
}

// Gebe eine int Zahl ein und liefere diese als Ergebnis
int enter_int ( std::string s )
{
  std::cout << s;
  int x;
  std::cin >> x;
  return x;
}

// Lese Argument i als int-Zahl ein
int readarg_int ( int argc, char *argv[], int i )
{
  if ( argc < i+1 )
  {
    std::cout << "Kann Argument " << i << " nicht lesen: zuwenig Argumente" << std::endl;
    exit(0);
  }

  int x; 
  sscanf( argv[i], "%d", &x );
  return x;
}

// Lese Argument i als double-Zahl ein
double readarg_double ( int argc, char *argv[], int i )
{
  if ( argc < i+1 )
  {
    std::cout << "Kann Argument " << i << " nicht lesen: zuwenig Argumente" << std::endl;
    exit(0);
  }

  double x; 
  sscanf( argv[i], "%lg", &x );
  return x;
}
