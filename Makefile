# Makefile for FTP Client Project

# Compiler
JAVAC = javac

# JAR tool
JAR = jar

# Source directory
SRC_DIR = src

# Build directory
BUILD_DIR = build

# Classes directory
CLASSES_DIR = $(BUILD_DIR)/classes

# JAR file
JAR_FILE = $(BUILD_DIR)/libs/4700ftp.jar

# Main class
MAIN_CLASS = Main

# Find all Java source files
SOURCES = $(shell find $(SRC_DIR) -name '*.java')

# Default target
all: $(JAR_FILE)

# Compile Java sources
$(CLASSES_DIR): $(SOURCES)
	@mkdir -p $(CLASSES_DIR)
	$(JAVAC) -d $(CLASSES_DIR) $(SOURCES)

# Create JAR file
$(JAR_FILE): $(CLASSES_DIR)
	@mkdir -p $(BUILD_DIR)/libs
	$(JAR) cfe $(JAR_FILE) $(MAIN_CLASS) -C $(CLASSES_DIR) .

# Clean build artifacts
clean:
	@rm -rf $(BUILD_DIR)

# Print help information
help:
	@echo "Available targets:"
	@echo "  all    : Compile and package the application (default)"
	@echo "  clean  : Remove build artifacts"
	@echo "  help   : Print this help information"

.PHONY: all clean run help