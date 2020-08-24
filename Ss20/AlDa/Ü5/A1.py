import pytest
import random

class SearchTree:
    class Node:
        def __init__(self, key, value):
            self._key = key
            self._value = value
            self._left = self._right = None

    def __init__(self):
        self._root = None
        self._size = 0

    def __len__(self):
        return self._size

    def depth(self, node = None,depth = 0):
        if(depth == 0):
            node = self._root
        if(node == None):
            return depth
        print(node._value)
        return max([self.depth(node._right, depth+1), self.depth(node._left, depth+1)])

    def __getitem__(self, key):          # implements 'value = tree[key]'
        if(len(self) == 0):
            raise KeyError("Key not in tree")
        n = self._root
        while(True):
            if(n._key == key):
                return n._value
            if(n._key > key):
                if(n._left == None):
                    raise KeyError("Key not in tree")
                n = n._left
            else:
                if(n._right == None):
                    raise KeyError("Key not in tree")
                n = n._right



    def __setitem__(self, key, value):   # implements 'tree[key] = value'
        inN = self.Node(key,value)
        if(len(self) == 0):
            self._root = inN
            self._size +=1
        else:
            n = self._root
            while(True):
                if( inN._key == n._key):
                    n._value = inN._value
                    break
                if( inN._key < n._key):
                    if(n._left == None):
                        n._left = inN
                        self._size +=1
                        break
                    else:
                        n = n._left
                if(inN._key > n._key):
                    if(n._right == None):
                        n._right = inN
                        self._size +=1
                        break
                    else:
                        n = n._right


    def getAllKeys(self,node = None, list = []):
        if(len(self) != 0):
            if(node == None):
                node = self._root
                list = []
            list.append([node._key, node._value])
            if(node._left != None):
                self.getAllKeys(node._left,list)
            if(node._right != None):
                self.getAllKeys(node._right,list)
            return list
        else:
            return []

    def __delitem__(self, key):          # implements 'del tree[key] '
        nodes = (self.getAllKeys())
        print(nodes)
        contains = False
        for l in nodes:
            if key == l[0]:
                nodes.remove(l)
                contains = True
        if(contains == False):
            raise KeyError("Key not in list")
        print(nodes)
        newTree = SearchTree()
        for l in nodes:
            newTree[l[0]] = l[1]

        self._size = newTree._size
        self._root = newTree._root


    @staticmethod
    def _tree_find(node, key):           # internal implementation
        ... # your code here

    @staticmethod
    def _tree_insert(node, key, value):  # internal implementation
        ... # your code here

    @staticmethod
    def _tree_remove(node, key):         # internal implementation
        ... # your code here

def test_search_tree():
    t = SearchTree()
    assert len(t) == 0

    ... # your code here
tree = SearchTree()



def test_search_tree():
    t = SearchTree()
    assert len(t) == 0

    # Test random insert on empty tree
    shuffled_range = list(range(5))
    random.shuffle(shuffled_range)

    print(shuffled_range)

    for i in shuffled_range:
        previous_length = len(t)
        t[i] = i
        assert len(t) == previous_length + 1
        assert t[i] == i

    for i in shuffled_range:
        previous_length = len(t)
        del t[i]
        assert len(t) == previous_length - 1
        print(i)
        with pytest.raises(KeyError):
            print(t[i])

'''
tree.drawTree()

tree[2]="vier"
tree[1]="vier"
tree[4]="vier"
tree[3]="vier"
tree[0]="vier"

print(tree[4])

del tree[2]
print("2")
del tree[1]
print("1")
print(tree[4])
del tree[4]
print("4")
del tree[3]
print("3")
del tree[0]
print("0")

print("alles supi")
test_search_tree()'''

'''tree = SearchTree()
shuffled_range = list(range(5))
random.shuffle(shuffled_range)
for i in shuffled_range:
    tree[i] = i

del tree[4]'''


def test_search_tree():
    t = SearchTree()
    assert len(t) == 0

    # Test random insert on empty tree
    shuffled_range = list(range(100))
    random.shuffle(shuffled_range)

    for i in shuffled_range:
        previous_length = len(t)
        t[i] = i
        assert len(t) == previous_length + 1
        assert t[i] == i

    # Test delete on random tree
    for i in list(range(100)):
        previous_length = len(t)
        del t[i]
        assert len(t) == previous_length - 1
        with pytest.raises(KeyError):
            t[i]
        with pytest.raises(KeyError):
            del t[i]

    # Test delete on leaf
    t = SearchTree()
    assert len(t) == 0

    t[0] = 0
    t[1] = 1

    assert len(t) == 2
    del t[1]
    assert len(t) == 1
    with pytest.raises(KeyError):
        t[1]
    with pytest.raises(KeyError):
        del t[1]

    t[0]

    # Test delete on node with one child
    t = SearchTree()
    assert len(t) == 0

    t[0] = 0
    t[5] = 1
    t[-1] = -1
    t[4] = 4

    assert len(t) == 4
    del t[5]
    assert len(t) == 3
    with pytest.raises(KeyError):
        t[5]
    with pytest.raises(KeyError):
        del t[5]

    t[0]
    t[-1]
    t[4]

    # Test delete on node with two children
    t = SearchTree()
    assert len(t) == 0

    t[20] = 20
    t[13] = 13
    t[25] = 25
    t[7]= 7
    t[14] = 14
    t[13.9] = 13.9
    t[13.5] = 13.5
    t[19] = 19
    t[13.6] = 13.6
    t[13.7] = 13.7

    assert len(t) == 10
    del t[13]
    with pytest.raises(KeyError):
        t[13]
    with pytest.raises(KeyError):
        del t[13]

    t[20]
    t[25]
    t[7]
    t[14]
    t[13.9]
    t[13.5]
    t[19]
    t[13.6]
    t[13.7]
