# Ferma OrientDB Extensions

Provides wrappers useful for using OrientDB with Ferma.

**Licensed under the Apache Software License v2**

For support please use [Gitter](https://gitter.im/Syncleus/Ferma?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
or the [official Ferma mailing list](https://groups.google.com/a/syncleus.com/forum/#!forum/ferma-list).

## Dependency

To include OrientDB extensions to Ferma include the following Maven dependencies into your build.

```xml
<dependency>
    <groupId>com.syncleus.ferma</groupId>
    <artifactId>ferma</artifactId>
    <version>3.1.0</version>
</dependency>
<dependency>
    <groupId>com.syncleus.ferma</groupId>
    <artifactId>ferma-orientdb</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Examples

```java
    // Setup the orientdb graph factory from which the transaction factory will create transactions
    try (OrientGraphFactory graphFactory = new OrientGraphFactory("memory:tinkerpop")) {
        TxFactory graph = new OrientTransactionFactoryImpl(graphFactory, "com.syncleus.ferma.ext.orientdb.model");

        try (Tx tx = graph.tx()) {
            Person joe = tx.getGraph().addFramedVertex(Person.class);
            joe.setName("Joe");
            Person hugo = tx.getGraph().addFramedVertex(Person.class);
            hugo.setName("Hugo");

            // Both are mutal friends
            joe.addFriend(hugo);
            hugo.addFriend(joe);
            tx.success();
        }

        try (Tx tx = graph.tx()) {
            Iterator<? extends Person> it = tx.getGraph().getFramedVerticesExplicit(Person.class);
            Iterable<? extends Person> it2 = toIterable(it);
            for (Person p : it2) {
                System.out.println("Found person with name: " + p.getName());
            }
        }

        String result = graph.tx((tx) -> {
            Person hugo = tx.getGraph().getFramedVertices("name", "Hugo", Person.class).next();
            StringBuffer sb = new StringBuffer();
            sb.append("Hugo's friends:");

            for (Person p : hugo.getFriends()) {
                sb.append(" " + p.getName());
            }
            return sb.toString();
        });
        System.out.println(result);
    }
```

## Obtaining the Source

The official source repository for OrientDB extension is located in the Syncleus Github repository and can be cloned using the
following command.

```bash
git clone https://github.com/Syncleus/Ferma-OrientDB.git
```

