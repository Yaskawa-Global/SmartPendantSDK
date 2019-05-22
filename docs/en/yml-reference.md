# YML

YML (Yaskawa Markup Language) is a cross-platform declarative language for easily describing user-interface components and their layout.

## Types

Each YML type represents a geometric element on the screen.  Many types are items that are visually rendered - such as Rectangles, Buttons, Text labels and so on.  Some types have no visual rendering but influcence the layout of other items, such as Row and Column.

Each type has a set of properties used to control the look and behaviour.  Most types also emit events in response to changes - such as maipulation by the user.  For example, the `Button` item emits a `Clicked` event when it is clicked by the user.

Types exist in a static inheritance tree, whereby each type inherits all the properties of its immediate super-type (ancestor).  For example, an `Item` is the ancestor of many visual types and has properties `width` and `height`.  Hence, all descendants of Item also have associated `width` and `height` properties.

By declaring an instance of a YML type in your interface, it creates a concrete instance of the type with concrete values for each of its properties (many of which may be default values of none are explicitly provided).

For example:

```qml
Rectangle {
    id: myrect
    width: 60
    height: 30
    color: "orange"
}
```
![Rectangle](assets/images/RectangleOrange.png "Rectangle")


creates a concrete instance of a Rectangle to be rendered on the screen with the given values of the width and height properties.

Instances can be nested, creating dynamic parent-child relationships that dictate the order in which items are rendered on the screen.

For example:

```qml
Row {
    id: myrow
    Rectangle { width: 40; height: 40; color: "red" }
    Rectangle { width: 40; height: 40; color: "green" }
    Rectangle { width: 40; height: 40; color: "blue" }
}
```
![Row example](assets/images/RectanglesColored.png "Rectangles")


creates three `Rectangle`s where the `Row` `myrow` is the parent of all three.  The behaviour of `Row` is to position its children horizontally.

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

It is also possible to declare new YML types, including new properties.  For example, suppose our UI used a lot of red squares and we wanted to avoid repeatedly using Rectangle and the color property for every instance:

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
![Declarations example](assets/images/DeclarationsExample.png "Declarations example")

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
  * [CheckBox](#checkbox)
  * [ComboBox](#combobox)
  * [Image](#image)
  * [Column](#column)
  * [Row](#row)
  * [Stack](#stack)
  * [Item](#item)
  * [TabBar](#tabbar)
  * [TabButton](#tabbutton)
  * [TabPanel](#tabpanel)
  * [Utility](#utility)
  * [Panel](#panel)

----

### Rectangle

A rectangle shape with the given dimensions and color.

![Rectangle example](assets/images/RectangleControl.png "Rectangle")

Inherits: [Item](#item)

#### Properties 

  * `string color` - fills area with given color.  Accepts hex color descriptions, such as `"#ff0000"` or predefined color names `"red"`, `"blue"` etc.
  * `int radius` - radius of rounded corner (defaults to 0)
  * `string borderColor` - optional color of border 

#### Example

```qml
Rectangle {
    width: 100
    height: 50
    radius: 5
    color: "orange"
    borderColor: "purple"
    borderWidth: 2
}
```

----

### Text

Text as specified in selectable font and size.

Inherits: [Item](#item)

#### Properties

  * `string text` - the text to display
  * `string color` - text color
  * `string fontName` - name of font
  * `int fontSize` - text font size
  * `int valign` - vertical alignment within Item.  One of `Const.Top`, `Const.Center` (default), `Const.Bottom` (no effect unless height overridden as height defaults to height of text)
  * `int halign` - horizontal alignment within Item.  One of `Const.Left` (default), `Const.Center`, `Const.Right` (no effect unless width overridden as width defaults to width of text)

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
  * `int requiredMode` - one of `Const.Manual`, `Const.Auto` or `Const.Any` (default) - enabled if controller operation mode as specified
  * `int requiredServo` - one of `Const.On`, `Const.Off` or `Const.Any` (default) - enabled if controller servo power as specified
  * `string requiredAccess` - enabled if current pendant security access level is as specified.  `Const.[Monitoring|Operating|Editing|Managing|ManagingSafety]` 

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
  * `string label` - text label for the field (e.g. may be shown above the entry area)
  * `int requiredMode` - one of `Const.Manual`, `Const.Auto` or `Const.Any` (default) - enabled if controller operation mode as specified
  * `int requiredServo` - one of `Const.On`, `Const.Off` or `Const.Any` (default) - enabled if controller servo power as specified
  * `string requiredAccess` - enabled if current pendant security access level is as specified.  `Const.[Monitoring|Operating|Editing|Managing|ManagingSafety]` 

#### Events
  * TextEdited - the text was edited by the user
  * EditingFinished - editing was finished by pressing Enter/Save or navigating away from the field (unfocus)
  * Accepted - Enter/Save was clicked after editing

----

### CheckBox

A selectable option (binary checked/unchecked) with optional label text.

|Checked|Unchecked|
|--|:--:|
|![CheckBox checked](assets/images/CheckBoxControlChecked.png "CheckBox checked")|![CheckBox unchecked](assets/images/CheckBoxControlUnchecked.png "CheckBox unchecked")|

#### Properties
  * `string text` - label text
  * `bool checked` - is the box checked?
  * `int requiredMode` - one of `Const.Manual`, `Const.Auto` or `Const.Any` (default) - enabled if controller operation mode as specified
  * `int requiredServo` - one of `Const.On`, `Const.Off` or `Const.Any` (default) - enabled if controller servo power as specified
  * `string requiredAccess` - enabled if current pendant security access level is as specified.  `Const.[Monitoring|Operating|Editing|Managing|ManagingSafety]` 

#### Events
  * CheckedChanged - the check box was checked or unchecked

#### Example

```qml
CheckBox {
    id: mycheckbox
    text: "Enable function"
}
```

----

### ComboBox

A set of options, one of which is selected.  Presented as a drop-down menu of options, showing the currently selected option.

![ComboBox example](assets/images/ComboBoxControl.png "ComboBox")

#### Properties
  * `array options` - array/vector of strings - one for each option (defaults to the empty array `[]`)
  * `int requiredMode` - one of `Const.Manual`, `Const.Auto` or `Const.Any` (default) - enabled if controller operation mode as specified
  * `int requiredServo` - one of `Const.On`, `Const.Off` or `Const.Any` (default) - enabled if controller servo power as specified
  * `string requiredAccess` - enabled if current pendant security access level is as specified.  `Const.[Monitoring|Operating|Editing|Managing|ManagingSafety]` 

#### Events
  * Activated - the user selected one of the options

#### Example

```qml
ComboBox {
    id: myselector
    width: 200
    options: ["AAA", "BBB", "CCC"]
}
```

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
  * `align` - alignment of child elements.  One of `Const.Left`, `Const.Center` or `Const.Right`

----

### Row

Arranges child items horizontally.

Inherits: [Item](#item)

#### Properties

  * `int spacing` - horizontal space between children (default 0)
  * `align` - alignment of child elements.  One of `Const.Top`, `Const.Center` or `Const.Bottom`

----

### Stack

Arranges child items on top of each other, such that only the top one is visible, according to the `currentIndex`.

Inherits: [Item](#item)

#### Properties

  * `int count` - the number of child  Items (readonly)
  * `int currentIndex` - the currently selected content item 

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

### TabBar

Container for TabButton Items for each clickable tab of a TabPanel navigation stack.  See [TabPanel](#tabpanel) for an example.

Inherits: [Item](#item)

#### Properties 

  * `int currentIndex` - index of currently selected tab (associated [TabPanel](#tabpanel) `currentIndex` typically bound to this property)

----

### TabButton

The clickable tab button that causes its associated tab panel to be shown.  See [TabPanel](#tabpanel) for an example.

Inherits: [Item](#item)

#### Properties 

  * `string text` - the text to display as the tab label
  * `string color` - tab button label text color

#### Events

  * `Clicked` - emitted when clicked (pressed & released).  Will set the parent [TabBar](#tabbar) `currentIndex` appropriately.

----

### TabPanel

Set of Items, one per tab content.  These are arranged as a stack so that only one content item is visible at a time.

*Note:* The default tab panel packground is light colored, so contained items will use the 'light' theme (even if the TabPanel
itself is on a 'dark' themed Item such as [Panel](#panel)).

![TabPanel example](assets/images/TabPanelControl.png "TabPanel"){:width="320px"}

Inherits: [Item](#item)

#### Properties 

  * `item bar` - the id of the TabBar item used to select the visible tab content
  * `int count` - the number of child tab content Items (readonly)
  * `int currentIndex` - the currently selected content item (set automatically from the TabBar)

#### Example

```qml
Column { 
    spacing: 0 // ensure TabBar directly above TabPanel
    TabBar {
        id: mytabbar                
        // useful to have TabButton ids to hook Clicked events to know when
        //  tabs are selected
        TabButton { id: tab1; text: "One" }
        TabButton { id: tab2; text: "Two" }
        TabButton { id: tab3; text: "Three" }
    }

    TabPanel {
        bar: mytabbar  // content selected by this referenced TabBar              
        width: 400
        height: 300

        Column {
            Label { text: "Tab Content One" }
            Button { text: "button1" }
        }
        Column {
            Label { text: "Tab Content Two" }
            Button { text: "button2" }
        }
        Column {
            Label { text: "Tab Content Three" }
            Button { text: "button3" }
        }

    }
}
```


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

