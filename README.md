# Ferma OrientDB Extensions

This extension provides various wrappers and abstract classes that are very useful when using OrientDB with Ferma.


```java
  OrientGraphFactory graphFactory = new OrientGraphFactory("memory:tinkerpop").setupPool(4, 10);
  OrientDBTxFactory graph = new OrientDBTxFactory(graphFactory, Vertx.vertx());
  
  try (Tx tx = graph.tx()) {
     Person p = tx.getGraph().addFramedVertex(Person.class);
     tx.success();
  }
```
