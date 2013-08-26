package com.koding.linkedlist

import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.RelationshipType

// Linked list with fixed head and tail nodes.
object LinkedList {

  // Points from head node and each entry node to tail node.
  object LINKED_LIST_TAIL extends RelationshipType { def name: String = "LINKED_LIST_TAIL" }

  // Points from head or entry node to next entry node or tail node.
  object LINKED_LIST_NEXT extends RelationshipType { def name: String = "LINKED_LIST_NEXT" }

  // Initializes a linked list with given head and tail nodes.
  def init(head: Node, tail: Node) {
    head.createRelationshipTo(tail, LINKED_LIST_TAIL)
    tail.createRelationshipTo(tail, LINKED_LIST_TAIL)
    head.createRelationshipTo(tail, LINKED_LIST_NEXT)
  }

  // Inserts entry between previous and the following node.
  def insertAfter(previous: Node, entry: Node): Unit = {
    val tail = previous.getSingleRelationship(LINKED_LIST_TAIL, Direction.OUTGOING).getEndNode
    entry.createRelationshipTo(tail, LINKED_LIST_TAIL)

    val nextRel = previous.getSingleRelationship(LINKED_LIST_NEXT, Direction.OUTGOING)
    nextRel.delete

    previous.createRelationshipTo(entry, LINKED_LIST_NEXT)
    entry.createRelationshipTo(nextRel.getEndNode, LINKED_LIST_NEXT)
  }

  // Inserts entry between next and the node before.
  def insertBefore(next: Node, entry: Node): Unit = {
    val tail = next.getSingleRelationship(LINKED_LIST_TAIL, Direction.OUTGOING).getEndNode
    entry.createRelationshipTo(tail, LINKED_LIST_TAIL)

    val nextRel = next.getSingleRelationship(LINKED_LIST_NEXT, Direction.INCOMING)
    nextRel.delete

    nextRel.getStartNode.createRelationshipTo(entry, LINKED_LIST_NEXT)
    entry.createRelationshipTo(next, LINKED_LIST_NEXT)
  }

  // Remove entry and return tail node. Do not execute on head or tail node.
  def remove(entry: Node) = {
    val tailRel = entry.getSingleRelationship(LINKED_LIST_TAIL, Direction.OUTGOING)
    val outgoingNextRel = entry.getSingleRelationship(LINKED_LIST_NEXT, Direction.OUTGOING)
    val incomingNextRel = entry.getSingleRelationship(LINKED_LIST_NEXT, Direction.INCOMING)

    tailRel.delete
    outgoingNextRel.delete
    incomingNextRel.delete
    incomingNextRel.getStartNode.createRelationshipTo(outgoingNextRel.getEndNode, LINKED_LIST_NEXT)

    tailRel.getEndNode
  }

  // Get previous entry. Do not execute on head node.
  def getPrevious(entry: Node) = {
    entry.getSingleRelationship(LINKED_LIST_NEXT, Direction.INCOMING).getStartNode
  }

  // Get all previous entries (including current, excluding head).
  def getAll(current: Node): List[Node] = {
    val nextRel = current.getSingleRelationship(LINKED_LIST_NEXT, Direction.INCOMING)
    if (nextRel == null) {
      Nil
    } else {
      current :: getAll(nextRel.getStartNode)
    }
  }

  // Searches backwards from tail until filter returns true and returns the selected node.
  // The filter must always return true for head node.
  def find(tail: Node, filter: (Node) => Boolean): Node = {
    val previous = getPrevious(tail)
    if (filter(previous)) {
      previous
    } else {
      find(previous, filter)
    }
  }

}