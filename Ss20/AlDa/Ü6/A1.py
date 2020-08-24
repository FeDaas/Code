#---------------------------------------------------------------------------------------------------------------------#
#                                                                                                                     #
# IAD Übungsblatt 6, Aufgabe 1 - Bernhard, Daas                                                                       #
#                                                                                                                     #
#---------------------------------------------------------------------------------------------------------------------#

import pytest
import random

#---------------------------------------------------------------------------------------------------------------------#

class BaseTreap:


    class Node:
        def __init__(self, key, value, priority):
            self._key = key
            self._value = value
            self._left = self._right = None
            self._priority = priority

        def __eq__(self,other):
            if(other != None):
                if(self._key == other._key and self._value == other._value and self._priority == other._priority):
                    return True
                return False
            return False

        def __ne__(self, other):
            return not self == other



    def __init__(self):
        self._root = None
        self._size = 0
        self._is_dynamic = False

    def __len__(self):
        return self._size

    def __getitem__(self, key):          # implements 'value = tree[key]'
        if self._size == 0:
            raise KeyError("Getitem on empty tree.")

        self._root, found_node = self._tree_find(self._root, key, self._is_dynamic)
        return found_node._value

    def __setitem__(self, key, value):   # implements 'tree[key] = value'
        self._root, was_insertion = self._tree_insert(self._root, key, value, self._is_dynamic)
        if was_insertion:
            self._size += 1

    def __eq__(self,other):
        return self.isEqualto(self, self._root, other._root)

    def __delitem__(self, key):          # implements 'del tree[key] '
        if self._size == 0:
            raise KeyError("Delete on empty tree.")

        self._root = self._tree_remove(self._root, key)
        self._size -= 1

    def depth(self):
        return BaseTreap._tree_depth(self._root)

    @staticmethod
    def isEqualto(self,node1, node2):
        if(node1 != node2):
            return False
        if(node1._right != None):
            if(node2._right != None):
                return self.isEqualto(self, node1._right, node2._right)
            else:
                return False
        if(node1._left != None):
            if(node2._left != None):
                return self.isEqualto(self, node1._right, node2._right)
            else:
                return False
        return True


    @staticmethod
    def _tree_depth(node, depth=0):
        if node == None:
            return depth

        return max([BaseTreap._tree_depth(node._left, depth + 1), BaseTreap._tree_depth(node._right, depth + 1)])

    @staticmethod
    def _tree_find(node, key, is_dynamic):           # internal implementation
        if node == None:
            raise KeyError("Key not found.")

        if node._key == key:
            if not is_dynamic:
                node._priority += 1
            return node, node

        if node._key < key:
            node._right, found_node = BaseTreap._tree_find(node._right, key, is_dynamic)
            if node._priority < node._right._priority:
                node = BaseTreap._tree_rotate_left(node)
            return node, found_node

        if node._key > key:
            node._left, found_node = BaseTreap._tree_find(node._left, key, is_dynamic)
            if node._priority < node._left._priority:
                node = BaseTreap._tree_rotate_right(node)
            return node, found_node

    @staticmethod
    def _tree_insert(node, key, value, is_dynamic):  # internal implementation
        if node == None:
            return BaseTreap.Node(key, value, random.randint(0, 1000) if is_dynamic else 0), True

        if node._key == key:
            node._value = value
            if not is_dynamic:
                node._priority += 1
            return node, False

        if node._key < key:
            if node._right == None:
                node._right = BaseTreap.Node(key, value, random.randint(0, 1000) if is_dynamic else 0)
                if node._priority < node._right._priority:
                    node = BaseTreap._tree_rotate_left(node)
                return node, True

            node._right, was_insertion = BaseTreap._tree_insert(node._right, key, value, is_dynamic)
            if node._priority < node._right._priority:
                node = BaseTreap._tree_rotate_left(node)
            return node, was_insertion

        if node._key > key:
            if node._left == None:
                node._left = BaseTreap.Node(key, value, random.randint(0, 1000) if is_dynamic else 0)
                if node._priority < node._left._priority:
                    node = BaseTreap._tree_rotate_right(node)
                return node, True

            node._left, was_insertion = BaseTreap._tree_insert(node._left, key, value, is_dynamic)
            if node._priority < node._left._priority:
                node = BaseTreap._tree_rotate_right(node)
            return node, was_insertion

    @staticmethod
    def _tree_remove(node, key):         # internal implementation
        if node == None:
            raise KeyError(f"Key \'{key}\' not found.")

        if node._key < key:
            node._right = BaseTreap._tree_remove(node._right, key)
            return node

        if node._key > key:
            node._left = BaseTreap._tree_remove(node._left, key)
            return node

        if node._key == key:
            if node._left == None:
                return node._right

            elif node._right == None:
                return node._left

            else:
                if node._left._priority > node._right._priority:
                    node = BaseTreap._tree_rotate_right(node)
                    node = BaseTreap._tree_remove(node, key)
                else:
                    node = BaseTreap._tree_rotate_left(node)
                    node = BaseTreap._tree_remove(node, key)
            return node

    @staticmethod
    def _tree_rotate_left(old_root):
        right_child = old_root._right
        if right_child == None:
            raise Exception("Left rotate with empty right child.")
        old_root._right = right_child._left
        right_child._left = old_root
        return right_child

    @staticmethod
    def _tree_rotate_right(old_root):
        left_child = old_root._left
        if left_child == None:
            raise Exception("Left rotate with empty left child.")
        old_root._left = left_child._right
        left_child._right = old_root
        return left_child

#---------------------------------------------------------------------------------------------------------------------#

class DynamicTreap(BaseTreap):

    def __init__(self):
        self._is_dynamic = True
        BaseTreap.__init__(self)


class RandomTreap(BaseTreap):

    def __init__(self):
        self._is_dynamic = False
        BaseTreap.__init__(self)

#---------------------------------------------------------------------------------------------------------------------#

    @staticmethod
    def _tree_remove(node, key):         # internal implementation
        if node == None:
            raise KeyError(f"Key \'{key}\' not found.")

        if node._key < key:
            node._right = BaseTreap._tree_remove(node._right, key)
            return node

        if node._key > key:
            node._left = BaseTreap._tree_remove(node._left, key)
            return node

        if node._key == key:
            if node._left == None:
                return node._right

            elif node._right == None:
                return node._left

            else:
                if node._left._priority > node._right._priority:
                    node = BaseTreap._tree_rotate_right(node)
                    node = BaseTreap._tree_remove(node, key)
                else:
                    node = BaseTreap._tree_rotate_left(node)
                    node = BaseTreap._tree_remove(node, key)
                return node

def test_random_treap():
    t = RandomTreap()
    assert len(t) == 0

    # Test random insert on empty tree
    shuffled_range = list(range(100))
    random.shuffle(shuffled_range)
    check_insert(t, shuffled_range)

    # Test random remove
    random.shuffle(shuffled_range)
    check_remove(t, shuffled_range)


def test_dynamic_treap():
    t = DynamicTreap()
    assert len(t) == 0

    # Test random insert on empty tree
    shuffled_range = list(range(100))
    random.shuffle(shuffled_range)
    check_insert(t, shuffled_range)

    # Test random remove
    random.shuffle(shuffled_range)
    check_remove(t, shuffled_range)

    # Test dynamic priorities
    t = DynamicTreap()
    assert len(t) == 0
    for i in range(100):
        t[i] = i
    t[50], t[50], t[50], t[20], t[20], t[10]
    t._root, node1 = t._tree_find(t._root, 50, True)
    t._root, node2 = t._tree_find(t._root, 20, True)
    t._root, node3 = t._tree_find(t._root, 10, True)
    t._root, node4 = t._tree_find(t._root, 5, True)
    assert node1._priority == 4
    assert node2._priority == 3
    assert node3._priority == 2
    assert node4._priority == 1


def check_insert(t, values):
    for i in range(len(values)):
        previous_length = len(t)
        t[values[i]] = values[i]
        check_find(t, values[:i+1])
        traverse_treap_and_check_treap_condition(t._root)
        assert len(t) == previous_length + 1


def check_remove(t, values):
    for i in range(len(values)):
        previous_length = len(t)
        del t[values[i]]
        check_find(t, values[i+1:])
        traverse_treap_and_check_treap_condition(t._root)
        assert len(t) == previous_length - 1
        with pytest.raises(KeyError):
            t[values[i]]
        with pytest.raises(KeyError):
            del t[values[i]]


def check_find(t, values):
    for value in values:
        t[value]


def check_insert(t, values):
    for i in range(len(values)):
        previous_length = len(t)
        t[values[i]] = values[i]
        check_find(t, values[:i+1])
        traverse_treap_and_check_treap_condition(t._root)
        assert len(t) == previous_length + 1


def check_remove(t, values):
    for i in range(len(values)):
        previous_length = len(t)
        del t[values[i]]
        check_find(t, values[i+1:])
        traverse_treap_and_check_treap_condition(t._root)
        assert len(t) == previous_length - 1
        with pytest.raises(KeyError):
            t[values[i]]
        with pytest.raises(KeyError):
            del t[values[i]]


def check_find(t, values):
    for value in values:
        t[value]


def traverse_treap_and_check_treap_condition(node):
    if node == None:
        return
    if node._left != None:
        if node._priority < node._left._priority:
            raise Exception(f"Heap condition not met since {node._priority} < {node._left._priority}.")
        if node._key <= node._left._key:
            raise Exception(f"Search tree condition not met since {node._key} <= {node._left._key}.")
        traverse_treap_and_check_treap_condition(node._left)

    if node._right != None:
        if node._priority < node._right._priority:
            raise Exception(f"Heap condition not met since {node._priority} < {node._right._priority}.")
        if node._key >= node._right._key:
            raise Exception(f"Search tree condition not met since {node._key} <= {node._right._key}.")
        traverse_treap_and_check_treap_condition(node._right)

def getMuscetier():
    filename = "die-drei-musketiere.txt"
    s = open(filename, encoding = "latin-1").read()
    for k in ',;.:-"\'!?"':
        s = s.replace(k, '')
    s = s.lower()
    text = s.split()

    rt = RandomTreap()
    dt = DynamicTreap()
    for word in text:
        rt[word] = None
        dt[word] = None

    return rt,dt

def compare_trees(tree1, tree2):
    return tree1 == tree2

rt,dt = getMuscetier()

print(compare_trees(rt,dt))

rt2 = RandomTreap()
dt2 = DynamicTreap()

rt2[0] = "null"
rt2[1] = "eins"
rt2[3] = "drei"

rt2[3] = "nul"
rt2[2] = "eind"
rt2[1] = "srei"

print(compare_trees(rt2,dt2))
