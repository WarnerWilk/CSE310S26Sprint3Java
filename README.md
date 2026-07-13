# Java Language

This repository contains a collection of console-based Java programs created to familiarize myself with the Java programming language by recreating previous assignments. The project was developed using AI assistance. Currently, the repository features two main programs: a Mad Libs generator that parses text templates and a point-of-sale Shopping Cart system that tracks inventory and records sales to JSON files.

## Instructions for Build and Use

Steps to build and/or run the software:

1. Ensure you have the Java Development Kit (JDK) installed on your system.
2. Open the repository folder in Visual Studio Code.
3. Compile the Java files using the terminal command `javac MadLibGame.java ShoppingCart.java` or use the VSCode Java extension's "Run" button.
4. Run the desired program using `java MadLibGame` or `java ShoppingCart`.

Instructions for using the software:

1. **Mad Libs Game**: Ensure a file named `madlib_templates.txt` is in the same directory. Run the program and follow the console prompts to enter various parts of speech (nouns, verbs, adjectives) to generate a silly story.


2. **Shopping Cart (Stocking)**: Launch the Shopping Cart program and enter `0` for Stocking Mode. You will be prompted to create a numeric passcode. Use this mode to set item names, quantities, and prices, which will save to `inventory.json`.


3. **Shopping Cart (Shopping)**: Enter `1` for Shopping Mode to browse available inventory and add items to your cart. Enter `0` when finished to view your receipt, calculate a 6% tax, and automatically log the transaction to `sales.json`.



## Development Environment

To recreate the development environment, you need the following software and/or libraries with the specified versions:

* Visual Studio Code (VSCode)
* Java Extension Pack for VSCode (Optional but recommended)
* Java Development Kit (JDK) 11 or newer

## Useful Websites to Learn More

I found these websites useful in developing this software:

* *N/A - This project was developed entirely with the assistance of AI, so external reference websites were not used.*

## Future Work

The following items I plan to fix, improve, and/or add to this project in the future:

* [ ] Create a calendar printer program.
* [ ] Develop an algorithm to determine optimal house placement for a player in Monopoly.
* [ ] Add robust error handling if `madlib_templates.txt` is improperly formatted.
