#include "fcpp.hh"
#include <string>  // std::string
#include <cstring> // strlen




// zur Unterscheidung von Elementen mit Operatoren und Variablen
char EMPTY_CHAR = ' ';

// zum Zusammenbauen von Strings
std::string convert_int_to_str(int zahl)
{
  char result[16];
  sprintf ( result, "%d", zahl );
  return result;
}

// zum Zusammenbauen von Strings
std::string convert_char_to_str(char zeichen)
{
  return std::string(1, zeichen);
}

struct BaumElement 
{
  int zahl;
  char variable;
  char zeichen;
  BaumElement* links;
  BaumElement* rechts;
};

std::string print_in(BaumElement* be) 
{  
  // TODO
}

std::string print_pre(BaumElement* be) 
{  
  // TODO
}

std::string print_post(BaumElement* be) 
{  
  // TODO
}

BaumElement* insert(BaumElement* expr, char var, BaumElement* subexpr) 
{
  // TODO
}

// Stack Implementation zum Baum generieren
struct Stack
{
  BaumElement* value[100];
  int top;
};

Stack stack;

void init_stack() 
{
  stack.top = 0;
}

void push(BaumElement* element)
{
  stack.value[stack.top] = element;
  stack.top++;
}

BaumElement* pop()
{
  stack.top--;
  return stack.value[stack.top];
}

BaumElement* create_tree(const char* ausdruck)
{
  // TODO
}

int main (int argc, char* argv[])
{
  const char* ausdruck1 = "67 55 - 54 6 / + 2 *";
  BaumElement* baum1 = create_tree(ausdruck1);

  if(baum1) 
  {
    std::cout << "Praefix: \n" << print_pre(baum1) << std::endl;
    std::cout << "Infix: \n" << print_in(baum1) << std::endl;
    std::cout << "Postfix: \n" << print_post(baum1) << std::endl;
  }

  const char* ausdruck2 = "X 1 - X 1 + *";
  BaumElement* baum2 = create_tree(ausdruck2);

  if(baum2) 
  {
    std::cout << "Praefix: \n" << print_pre(baum2) << std::endl;
    std::cout << "Infix: \n" << print_in(baum2) << std::endl;
    std::cout << "Postfix: \n" << print_post(baum2) << std::endl;
  }

  baum2 = insert(baum2, 'X', baum1);

  if(baum2) 
  {
    std::cout << "Praefix: \n" << print_pre(baum2) << std::endl;
    std::cout << "Infix: \n" << print_in(baum2) << std::endl;
    std::cout << "Postfix: \n" << print_post(baum2) << std::endl;
  }

  return 0;
}
