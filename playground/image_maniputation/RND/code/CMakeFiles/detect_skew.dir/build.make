# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 2.8

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list

# Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/siddhant/Downloads/RND/code

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/siddhant/Downloads/RND/code

# Include any dependencies generated for this target.
include CMakeFiles/detect_skew.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/detect_skew.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/detect_skew.dir/flags.make

CMakeFiles/detect_skew.dir/detect_skew.cpp.o: CMakeFiles/detect_skew.dir/flags.make
CMakeFiles/detect_skew.dir/detect_skew.cpp.o: detect_skew.cpp
	$(CMAKE_COMMAND) -E cmake_progress_report /home/siddhant/Downloads/RND/code/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building CXX object CMakeFiles/detect_skew.dir/detect_skew.cpp.o"
	/usr/bin/c++   $(CXX_DEFINES) $(CXX_FLAGS) -o CMakeFiles/detect_skew.dir/detect_skew.cpp.o -c /home/siddhant/Downloads/RND/code/detect_skew.cpp

CMakeFiles/detect_skew.dir/detect_skew.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/detect_skew.dir/detect_skew.cpp.i"
	/usr/bin/c++  $(CXX_DEFINES) $(CXX_FLAGS) -E /home/siddhant/Downloads/RND/code/detect_skew.cpp > CMakeFiles/detect_skew.dir/detect_skew.cpp.i

CMakeFiles/detect_skew.dir/detect_skew.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/detect_skew.dir/detect_skew.cpp.s"
	/usr/bin/c++  $(CXX_DEFINES) $(CXX_FLAGS) -S /home/siddhant/Downloads/RND/code/detect_skew.cpp -o CMakeFiles/detect_skew.dir/detect_skew.cpp.s

CMakeFiles/detect_skew.dir/detect_skew.cpp.o.requires:
.PHONY : CMakeFiles/detect_skew.dir/detect_skew.cpp.o.requires

CMakeFiles/detect_skew.dir/detect_skew.cpp.o.provides: CMakeFiles/detect_skew.dir/detect_skew.cpp.o.requires
	$(MAKE) -f CMakeFiles/detect_skew.dir/build.make CMakeFiles/detect_skew.dir/detect_skew.cpp.o.provides.build
.PHONY : CMakeFiles/detect_skew.dir/detect_skew.cpp.o.provides

CMakeFiles/detect_skew.dir/detect_skew.cpp.o.provides.build: CMakeFiles/detect_skew.dir/detect_skew.cpp.o

# Object files for target detect_skew
detect_skew_OBJECTS = \
"CMakeFiles/detect_skew.dir/detect_skew.cpp.o"

# External object files for target detect_skew
detect_skew_EXTERNAL_OBJECTS =

detect_skew: CMakeFiles/detect_skew.dir/detect_skew.cpp.o
detect_skew: CMakeFiles/detect_skew.dir/build.make
detect_skew: CMakeFiles/detect_skew.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking CXX executable detect_skew"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/detect_skew.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/detect_skew.dir/build: detect_skew
.PHONY : CMakeFiles/detect_skew.dir/build

CMakeFiles/detect_skew.dir/requires: CMakeFiles/detect_skew.dir/detect_skew.cpp.o.requires
.PHONY : CMakeFiles/detect_skew.dir/requires

CMakeFiles/detect_skew.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/detect_skew.dir/cmake_clean.cmake
.PHONY : CMakeFiles/detect_skew.dir/clean

CMakeFiles/detect_skew.dir/depend:
	cd /home/siddhant/Downloads/RND/code && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/siddhant/Downloads/RND/code /home/siddhant/Downloads/RND/code /home/siddhant/Downloads/RND/code /home/siddhant/Downloads/RND/code /home/siddhant/Downloads/RND/code/CMakeFiles/detect_skew.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/detect_skew.dir/depend

