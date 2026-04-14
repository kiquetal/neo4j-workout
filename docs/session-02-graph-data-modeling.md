### 3. Relationship → Intermediate Node (Reification)

This is a powerful pattern for when a simple connection isn't enough.

**When to use it:** You should create a specialized intermediate node when the **act or event of connecting two nodes needs its own identity**. This is common when you want to:
- Add extensive properties to the connection (e.g., a review comment, a timestamp, a rating).
- Connect *other nodes* to the connection itself (e.g., someone "liking" a review).

**Before:** A simple relationship with a property.
`(:User)-[:RATED {score: 5}]->(:Movie)`

```ascii
              rating: 5
           +-----------------+
           |     RATED       |
( User )---/                   \--->( Movie )
           +-----------------+

```

**After:** The relationship is "reified" (turned into) a `Review` node.
`(:User)-[:WROTE]->(:Review {score: 5, text: '...'})-[:REVIEWS]->(:Movie)`

```ascii
           +-----------------+     +-----------------+
           |      WROTE      |     |     REVIEWS     |
( User )---/                   \--->(  Review     )---/                   \--->( Movie )
           +-----------------+     | score: 5      |     +-----------------+
                                   | text: '...'   |
                                   +---------------+
                                          ^
                                          |
                                   +------|------+
                                   |    LIKED    |
                                 ( Other User )

```

Why: The new `:Review` node can now hold more properties and can have its own relationships, like being "liked" by another user.