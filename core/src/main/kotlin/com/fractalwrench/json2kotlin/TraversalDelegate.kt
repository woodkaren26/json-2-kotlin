package com.fractalwrench.json2kotlin

import java.util.*

interface TraversalDelegate { // TODO remove
    fun processTreeLevel(levelQueue: LinkedList<TypedJsonElement>)
}