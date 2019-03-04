# YML

YML (Yaskawa Markup Language) is a cross-platform declarative language for easily describing user-interface components and their layout.

## Types

Each YML type represents a geometic element on the screen.  Many types are items that are visually rendered - such as Rectangles, Buttons, Text labels and so on.  Some types have no visual rendering but influcence the layout of other items, such as Row and Column.

Each type has a set of properties used to control the look and behaviour.  Most types also emit events in response to changes - such as maipulation by the user.  For example, the `Button` item emits a `Clicked` event when it is clicked by the user.

Types exist in a static inheritance tree, whereby each type inherits all the properties of its immediate super-type (ancestor).  For example, an `Item` is the ancestor of many visual types and has properties `width` and `height`.  Hence, all descendants of Item also have associated `width` and `height` properties.

By declaring an instance of a YML type in your interface, it creates a concrete instance of the type with concrete values for each of its properties (many of which may be default values of none are explicitly provided).

For example:

```qml
Rectangle {
    id: myrect
    width: 20
    height: 50
}
```

creates a concrete instance of a Rectangle to be rendered on the screen with the given values of the width and height properties.

Instances can be nested, creating dynamic parent-child relationships that dictate the order in which items are rendered on the screen.

For example:

```qml
Row {
    id: myrow
    Rectangle { width: 10; height: 10; color: "red" }
    Rectangle { width: 10; height: 10; color: "green" }
    Rectangle { width: 10; height: 10; color: "blue" }
}
```

creates three `Rectangle`s where the `Row` `myrow` is the parent of all three.  The behaviour `Row` is to position its children horizontally.

## Properties 

### Expressions 

The value supplied for a property, in addition to simple literal values, like `10` or `"red"`, can consist of expressions in a syntax very similar to Javascript.

Properties have a specific type - one of `bool`, `int`, `real` or `string`.  Each has a corresponding way to write a literal for that type:

  * `bool` - `true` or `false`
  * `int` - integers (32bit), including scientific notation: `10`, `-100`, `-3.4e8`
  * `real` - floating point values (IEEE754 double precision 64bits): `10.1`, `-0.001`
  * `string` - character strings enclosed in single or double quotes (Unicode UTF-8 encoding).  C-escape sequences are supported : `"red"`, `'今日は!'`, `"\n"`

Hence, a valid expression for a property value of type `int` (e.g. width) is `(20*3+(100-10))-1`.

As we saw above, each instance of a YML type can be given an `id`.  You can access the propery of a specific instance by prefixing the property names with the id and a period: `myrow.width`.  Note that the property value need not have been explicitly supplied, as is the case above.  

Properties can be referenced in expressions: `myrow.width*3 + 10 - myrect.width`, for example.

### Declarations

It is also possible to declare your YML types, including new properties.  For example, suppose our UI used a lot of red squares and we wanted to avoid repeatedly using Rectangle and the color property for every instance:

```qml
RedSquare : Rectangle 
{    
    width: 50
    height: width
    color: "red"

    property int area: width*height
}

...

Row {
    id: rowofsquares
    RedSquare { id: s1; width: 60 } // override width
    RedSquare { id: s2; width: 70}
    RedSquare { id: s3 }
    Text { 
        text: "Total Area = "+(s1.area + s2.area + s3.area)
    }
}
```

Notice:
  * Our new `RedSquare` type inherits from `Rectangle` and hence inherits its properties and behaviour as defaults
  * It declares a new property named `area`
  * Use of `//` C++-style single-line comments.  `/* ... */` C-style are also allowed.
  * Even though we override the `width`, the shape will still be square as the `height` of `RedSquare` is defined to be the `width`.  This makes use of a binding (described below).
  * Liberal automatic type conversion - the `int` expression for the areas was automatically converted to `string` in order to satisfy the `+` string concatenation operator

### Bindings

When we reference properties in an expression for the value of a property, the property is bound to the expression.  This means that changes in the values of referenced properties will be reflected in the property value.

For example, in `RedSquare`, any change, at run-time, of the width property will also change the height property because it is bound to width.  Bindings can be complex expressions, not just simple property references.

For example:

```qml
Rectangle {
    id: myrect
    width: 20
    height: 10
}

Text {    
    id: mytext
    text: "Area of myrect with margin of 10 is "+( (myrect.width+10) * (myrect.height+10))
}
```

will result in text being displayed that initially reads "Area of myrect with margin of 10 is 600", but will automatically update appropriately if the value of `myrect` `width` or `height` is assigned at run-time, because the expression for the property `text` is a binding that includes references to the myrect properties.

**Note**, however, if a property with a binding expression is explicitly assigned by extension code at run-time, the binding is lost and the last assigned value persists.  Hence, if your extension code assigned the value `"Hello, World"` to `mytext.text`, it will retain that value until explicitly reassigned regardless of myrect.


## Events

Events are how the UI signals to your extension the occurance of various actions and activities happening on the UI during run-time.  The types of events emitted is specific to the YML type.  For example, `Button` emits `Pressed`, `Released` and `Clicked` events corresponding to when the button is touched, when the touch is released and if the touch-release sequence signified a 'click' (e.g. touch & release both over the button touch area and `Button` wasn't disabled etc.).


# YML Markup Reference

This section lists each of the supported YML types, along with its properties and events.  Inherited properties are not duplicated.

## Items

  * [Rectangle](#rectangle)
  * [Text](#text)
  * [Label](#label)
  * [Button](#button)
  * [TextField](#textfield)
  * [Image](#image)
  * [Column](#column)
  * [Row](#row)
  * [Item](#item)
  * [Utility](#utility)
  * [Panel](#panel)

----

### Rectangle

A rectangle shape with the given dimensions and color.

Inherits: [Item](#item)

#### Properties 

  * `string color` - fills area with given color.  Accepts hex color descriptions, such as `"#ff0000"` or predefined color names `"red"`, `"blue"` etc.
  * `int radius` - radius of rounded corner (defaults to 0)
  * `string borderColor` - optiona color of border 
  * `string borderColor` - optiona color of border 

----

### Text

Text as specified in selectable font and size.

Inherits: [Item](#item)

#### Properties

  * `string text` - the text to display
  * `string color` - text color
  * `string fontName` - name of font
  * `int fontSize` - text font size

----

### Label

Text used as the label for a UI control.  Defaults to larger font size.

Inherits: [Text](#text)

----

### Button

Inherits: [Item](#item)

#### Properties

  * `string text` - the button label (inside the button)
  * `string icon` - icon inside the button (optional)
  * `bool checkable` - is this a toggle button? (toggles between checked and unchecked on each click) defaults to false.
  * `bool checked` - is the button initially checked (if checkable)

#### Events

  * `Clicked` - emitted after appropriate press & release
  * `Pressed` - the button was touched ('pushed down')
  * `Released` - the touch was released ('up')

----

### TextField

An field of text editable by the user.  When clicked/focused will cause the on-screen virtual keyboard or keypad to show.

#### Properties
  * `string text` - current value of the text field (defaults empty)
  * `string placeholderText` - placeholder text shown (lighter) prior to editing - hint to user what to enter
  * `string color` - color of the text
  * `string placeholderTextColor` - color of the placeholder text
  * `bool readOnly` - is the field editable?

#### Events
  * TextEdited - the text was edited by the user
  * EditingFinished - editing was finished by pressing Enter/Save or navigating away from the field (unfocus)
  * Accepted - Enter/Save was clicked after editing


----
### Image

On-screen image.  Must be registered though API `registerImageFile()` or `registerImageData()` functions prior to instantiation.

Inherits: [Item](#item)

#### Properties

  * `string source` - reference to previously registered image name

----

### Column

Arranges child items vertically.

Inherits: [Item](#item)

#### Properties

  * `int spacing` - vertical space between children (default 0)

----

### Row

Arranges child items horizontally.

Inherits: [Item](#item)

#### Properties

  * `int spacing` - horizontal space between children (default 0)

----

### Item

The ancestor of all geometric types (visual or not).

#### Properties

  * `int width` - the on-screen width
  * `int height` - the on-screen height
  * `int x` - the x coordinate (from left) relative to parent item
  * `int y` - the y coordinate (from top) relative to parent item
  * `bool visible` - is the item visible or hidden?

#### Events

  * `VisibleChanged` - change in the `visible` property

----

### Utility

The type of all pendant Utility Window items.

Inherits: [Item](#item)

#### Properties 

  * `string theme` - `light` or `dark` (defaults to light)

#### Events

  * `UtilityOpened` - window was opened
  * `UtilityClosed` - window was closed
  * `UtilityMoved` - windows was moved while open (including resized)

----

### Panel

The type of all lower-screen detail panel items.

Inherits: [Item](#item)

#### Properties 

  * `string theme` - `light` or `dark` (defaults to dark)

