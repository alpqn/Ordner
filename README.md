<div align="center">

<h1>Ordner</h1>
<img src="https://github.com/user-attachments/assets/e3d50080-e9bd-4609-b601-00ef9edc2417" width="300" alt="Centered image" />
<br>
<em>A simple, cross-platform and customizable data tracker.</em>
</div>

# Usage
- [Create a Template](#create-a-template)
- [Create a Record](#create-a-record)
- [Managing Records](#managing-records)
# Installation
Build with Java 24+ after cloning the repository
```bash
./gradlew build
```
And run:
```bash
./gradlew run
```
(Use gradlew.bat for Windows)
## Create a Template
1. Navigate to `New Template` in the status bar.
2. Set a template name (e.g. `Runs`).
3. Add members.
    1. Select a type (Type of input for this member, e.g. `Integer`).
    2. Set a name (Equivalent to a column name).

![2025-07-03-175239_1920x1080_scrot](https://github.com/user-attachments/assets/c71356ee-854a-43df-968a-4ec9179d2779)

## Create a Record
1. Navigate to `New Record` in the status bar.
2. Set a record name (e.g. `Germany_Marathons`).
3. Choose a template (The record will conform to it and create fields accordingly. Let's choose the `Runs` template we created).

![Create a Record](https://github.com/user-attachments/assets/0752b8e3-a12d-4c9a-ab05-43f239917bfc)
## Managing Records
- To add a new row, fill in all the text fields and hit `Add Row` (Make sure your input is valid for the field).
- You can edit single cells by double-clicking.
- You can resize the width of each column by dragging the column names at the top.
- You can sort the data by clicking on a column name.
- There are special values for some types:
    - For a `Date` field, entering `today` will automatically fill in the current date.
    - For a `Time` field, entering `now` will automatically fill in the current time.
    - For a `Timestamp` field, entering `now` will automatically fill in the current date-time value pair.

![Managing Records](https://github.com/user-attachments/assets/f70df259-f5ea-429d-b0aa-da536c30b070)
#### Other
- `Integer` and `Double` values can only hold 64-bit values.
