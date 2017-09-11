# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)


#insert correct paths here..
set(OZO_SDK_PATH ${CMAKE_SOURCE_DIR}/../SDK)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp

             # ozo sdk
             src/main/cpp/ozo/ozo.cpp
             )


target_include_directories( native-lib PRIVATE
                            src/main/cpp
                            "${OZO_SDK_PATH}")


target_link_libraries( # Specifies the target library.
                       native-lib
                       android
                       EGL
                       GLESv3
                       log
                       ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/libOZOPlayer.so
                       )


target_compile_definitions( native-lib PRIVATE -DRUNTIMELINK=1)


file(COPY "${ANDROID_NDK}/sources/cxx-stl/llvm-libc++/libs/${ANDROID_ABI}/libc++_shared.so" DESTINATION "${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/")

#hrumph android studio actually builds both debug and release so this copy does not work as expected.. (we get both debug/release libaries to the apk)
file(GLOB SDK_LIBRARIES "${OZO_SDK_PATH}/OZOPlayer/Lib/Android/${CMAKE_BUILD_TYPE}/${ANDROID_ABI}/*.so")
file(COPY ${SDK_LIBRARIES} DESTINATION "${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/")