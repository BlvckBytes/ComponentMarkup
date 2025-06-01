# ComponentMarkup

(Just a place of personal notes, for now.)

## Structural Attributes

Attributes with a leading `*` are structural, internal, non-extendable and reserved; they're always evaluated as compute-syntax. Also, maybe I should introduce a `<container>`-tag, for when structural directives are to be applied to multiple elements all at once; will just be a wrapper on the AST-level and not add anything to the final component.

### If-Else Conditionals

These structural attributes are grouped by layer, avoiding bloated syntax like `; else <ref>` on Angular.

```
<gold *if="user">Hello, {{user.name}}

<!-- New if after prior: entering a new group -->
<container *if="user.isAdmin">
  <red *if="settings.showToolsA">Tool A
  <green *else-if="settings.showToolsB">Tool B
  <blue *else>Not available
</container>
```

### For Loops

```
<!-- The trailing identifier after *for- makes for the iteration-variable -->
<container *for-member="members">
  <red>{{member}} at index {{loop.index}}
</container>
```

```
<!-- The loop-variable is always specific to the current scope and *will* be shadowed by design -->
<container *for-member="members" let-memberIndex="loop.index">
    <container *for-post="member.posts" let-postIndex="loop.index">
      <red>{{member}} at index {{memberIndex}} wrote {{post}} at index {{postIndex}}
    </container>
</container>
```
