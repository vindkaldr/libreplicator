/*
 *     Copyright (C) 2016  Mihaly Szabo <szmihaly91@gmail.com/>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplicator.journal

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.isEmptyString
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class DefaultFileHandlerTest {
    private val FIRST_LINE = "first-line"
    private val SECOND_LINE = "second-line"
    private val NEW_LINE = "new-line"

    private val NEW_DIRECTORY = "new-directory"

    private lateinit var testDirectory: Path

    private lateinit var file: Path
    private lateinit var emptyFile: Path
    private lateinit var notExistingFile: Path

    private lateinit var notExistingDirectory: Path

    private lateinit var fileHandler: FileHandler

    @Before
    fun setUp() {
        testDirectory = createTestDirectory()

        file = createFile()
        emptyFile = createEmptyFile()
        notExistingFile = initializeNotExistingFile()

        notExistingDirectory = initializeNotExistingDirectory()

        fileHandler = DefaultFileHandler()
    }

    @After
    fun tearDown() {
        removeTestDirectory()
    }

    @Test
    fun createDirectory_createsDirectories_whenParentDirectoryNotExists() {
        fileHandler.createDirectory(notExistingDirectory, NEW_DIRECTORY)

        val newParentDirectory = notExistingDirectory.toFile()
        assertThat(newParentDirectory.exists(), equalTo(true))
        assertThat(newParentDirectory.isDirectory, equalTo(true))

        val newDirectory = notExistingDirectory.resolve(NEW_DIRECTORY).toFile()
        assertThat(newDirectory.exists(), equalTo(true))
        assertThat(newDirectory.isDirectory, equalTo(true))
    }

    @Test
    fun createDirectory_createsDirectory() {
        fileHandler.createDirectory(testDirectory, NEW_DIRECTORY)

        val newDirectory = testDirectory.resolve(NEW_DIRECTORY).toFile()
        assertThat(newDirectory.exists(), equalTo(true))
        assertThat(newDirectory.isDirectory, equalTo(true))
    }

    @Test(expected = NoSuchFileException::class)
    fun readFirstLine_throwsException_whenFileNotExists() {
        fileHandler.readFirstLine(notExistingFile)
    }

    @Test
    fun readFistLine_returnsEmptyString_whenFileIsEmpty() {
        assertThat(fileHandler.readFirstLine(emptyFile), isEmptyString())
    }

    @Test
    fun readFirstLine_returnsFirstLine() {
        assertThat(fileHandler.readFirstLine(file), equalTo(FIRST_LINE))
    }

    @Test
    fun write_createsFileAndWritesLine_whenFileNotExists() {
        fileHandler.write(notExistingFile, FIRST_LINE)

        assertThat(Files.readAllLines(notExistingFile).first(), equalTo(FIRST_LINE))
    }

    @Test
    fun write_writesLineIntoEmptyFile() {
        fileHandler.write(emptyFile, FIRST_LINE)

        val lines = Files.readAllLines(emptyFile)

        assertThat(lines.size, equalTo(1))
        assertThat(lines.first(), equalTo(FIRST_LINE))
    }

    @Test
    fun write_truncatesExistingFileAndWritesLine() {
        fileHandler.write(file, NEW_LINE)

        val lines = Files.readAllLines(file)

        assertThat(lines.size, equalTo(1))
        assertThat(lines.first(), equalTo(NEW_LINE))
    }

    @Test(expected = NoSuchFileException::class)
    fun move_throwsException_whenSourceNotExists() {
        fileHandler.move(notExistingFile, emptyFile)
    }

    @Test
    fun move_movesFile() {
        fileHandler.move(file, emptyFile)

        assertThat(Files.readAllLines(emptyFile), equalTo(listOf(FIRST_LINE, SECOND_LINE)))
    }

    private fun createTestDirectory(): Path {
        return Files.createTempDirectory("libreplicator-test-file-handler-")
    }

    private fun createFile(): Path {
        val file = testDirectory.resolve("file")
        Files.write(file, listOf(FIRST_LINE, SECOND_LINE))
        return file
    }

    private fun createEmptyFile(): Path {
        val file = testDirectory.resolve("empty-file")
        file.toFile().createNewFile()
        return file
    }

    private fun initializeNotExistingFile(): Path {
        return testDirectory.resolve("not-existing-file")
    }

    private fun initializeNotExistingDirectory(): Path {
        return testDirectory.resolve("not-existing-directory")
    }

    private fun removeTestDirectory() {
        testDirectory.toFile().deleteRecursively()
    }
}
