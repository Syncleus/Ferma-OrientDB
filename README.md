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

## Vert.x Integration

Similar to ```Vertx.executeBlocking``` the method ```asyncTx``` can be used in combination with Vert.x. 
The given handler will be executed within a transaction and a dedicated worker pool thread. 
Please note that the transaction handler may be executed multiple times in order to retry the transaction code when an OConcurrentModificationException occurred. 

```java
  Future<Person> future = Future.future();
  graph.asyncTx(tx -> {
    Person p = tx.getGraph().addFramedVertex(Person.class);
    tx.complete(p);
  }, (AsyncResult<Person> rh) -> {
    future.complete(rh);
  });
```
