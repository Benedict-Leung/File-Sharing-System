# CSCI2020_Assignment2

# File sharing system

An application for sharing files between a server and client

![Preview](./FileSharing.png?raw=true)

# UI and System improvements
- Allows subdirectories to be shown
- Icons for folders and files
- Shows file size (B, kB, MB, GB, TB, PB, EB)
- Allows all file types to be uploaded and downloaded (text, exe, HTML, images, etc.)
- Shows what file is selected via a Label

<em><strong>Note:</strong> TreeItemSerialize.java is NOT my code (Look at reference)</em>

# Build Instructions for IntelliJ
### Prerequisites
- JDK 15.X.X+
- JavaFX 15.X.X+

### Clone
     git clone https://github.com/Benedict-Leung/CSCI2020_Assignment2

### Edit Configurations
Make sure to add the JavaFX library (`File -> Project Structure -> Libraries`) and add VM options (`Run -> Edit Configurations`) to IntelliJ

(VM options: `--module-path /path/to/javafx/sdk --add-modules javafx.controls,javafx.fxml`)

\
<em><strong>Note:</strong> First run Server.java, then run Main.java</em>

To add command arguments go to `Run -> Edit Configurations` and add to program arguments

# Reference
- https://www.jetbrains.com/help/idea/javafx.html#create-project - Reference to running JavaFX projects on IntelliJ
- https://stackoverflow.com/questions/61023834/how-do-i-make-javafx-treeview-and-treeitem-serializable - Reference to serializing a TreeView and TreeItem