import json

recipes = json.load(open("cocktails.json"))

def normalize_string(s):
    cleanString = []
    zutat = ""
    write = True
    #extract the ingredients
    for i in range(len(s)):
        for x in s[i]:
            for n in x:
                if(n == '(' or n == ','):
                    write = False
                if(write):
                    if (n not in '0123456789%()[]{}*'):
                        zutat += n.lower()

            if(zutat != "" and zutat != " "):
                cleanString.append(zutat.strip())
            zutat = ""
            write = True

    return cleanString

def all_ingredients(recipes):
    allIngredients = []
    ingredients = []
    for i in recipes:
        ingredients.append(recipes[i]["ingredients"].keys())
    ingredients = normalize_string(ingredients)
    allIngredients = set(ingredients)
    return allIngredients

def cocktails_inverse(recipes):
    data = json.load(open("cocktails.json"))
    inverse_recipes = {}
    for i in recipes:
        cocktails = []
        for cocktail in data:
            for zutat in (data[cocktail]["ingredients"].keys()):
                if(i == zutat.lower()[:len(i)+1].strip()):
                    if(cocktail not in cocktails):
                        cocktails.append(cocktail)
        inverse_recipes[i] = cocktails
    return inverse_recipes

def sort_dict_by_cocktail_count(dict):
    return sorted(dict.items(), key = lambda item: len(item[1]), reverse = True)

recipes = all_ingredients(recipes)
inverse_recipes = cocktails_inverse(recipes)

def possible_cocktails(inverse_recipes, available_ingredients):
    possible_cocktails = []
    recipes = json.load(open("cocktails.json"))


with open("cocktails_inverse.json", "w", encoding = "utf-8") as fp:
    json.dump(inverse_recipes, fp, indent = 4, ensure_ascii=False)

ingredients_sorted = sort_dict_by_cocktail_count(inverse_recipes)
for i in range(len(ingredients_sorted)):
    print(ingredients_sorted[i][0],":   ",len(ingredients_sorted[i][1]))
#print(ingredients_sorted)


'''for i in range(len(inverse_recipes)):
    if(len(inverse_recipes[i][1]) == 0):
        print(inverse_recipes[i][0],len(inverse_recipes[i][1]))'''
