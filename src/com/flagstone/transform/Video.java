/*
 * Video.java
 * Transform
 *
 * Copyright (c) 2001-2009 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;



/**
 * Video is a container class for the objects that represents the data
 * structures in a Flash video file. It is very similar to the Movie class for
 * flash (.swf) files and provides a simple API for decoding and encoding files
 * and accessing the objects that represent the different data structures used
 * for audio and video data.
 */
public final class Video implements Cloneable
{
	private static final String FORMAT = "Video: { signature=%s; version=%d; objects=%s ";
	
	protected String signature;
	protected int version;
	protected List<VideoTag> objects;

	/**
	 * Creates a Video object with no objects.
	 */
	public Video()
	{
		signature = "FLV";
		version = 1;
		objects = new ArrayList<VideoTag>();
	}

	public Video(Video object)
	{
		signature = object.signature;
		version = object.version;
		objects = new ArrayList<VideoTag>(object.objects.size());
		for (VideoTag tag : object.objects) {
			objects.add(tag.copy());
		}
	}

	/**
	 * Returns the number representing the version of Flash Video that the video
	 * represents.
	 */
	public int getVersion()
	{
		return version;
	}

	/**
	 * Sets the Flash Video version.
	 * 
	 * @param aNumber
	 *            the version of the Flash Video file format that this object
	 *            utilises.
	 */
	public void setVersion(int aNumber)
	{
		version = aNumber;
	}

	/**
	 * Returns the array of video objects.
	 */
	public List<VideoTag> getObjects()
	{
		return objects;
	}

	/**
	 * Sets the array of objects contained in the Video.
	 * 
	 * @param anArray
	 *            the array of objects that describe a coder. Must not be null.
	 */
	public void setObjects(List<VideoTag> anArray)
	{
		if (anArray == null) {
			throw new IllegalArgumentException(Strings.ARRAY_CANNOT_BE_NULL);
		}
		objects = anArray;
	}

	/**
	 * Adds the object to the Movie.
	 * 
	 * @param anObject
	 *            the object to be added to the coder. Must not be null.
	 */
	public Video add(VideoTag anObject)
	{
		if (anObject == null) {
			throw new IllegalArgumentException(Strings.OBJECT_CANNOT_BE_NULL);
		}
		objects.add(anObject);
		return this;
	}

	/**
	 * Adds the array of object to the Video.
	 * 
	 * @param array
	 *            an array of VideoTags that will be added to the video in
	 *            the order they are in the array. Must not be null.
	 */
	public Video add(List<VideoTag> array)
	{
		if (array == null) {
			throw new IllegalArgumentException(Strings.ARRAY_CANNOT_BE_NULL);
		}
		objects.addAll(array);
		return this;
	}

	/**
	 * Decodes the contents of the specified file. An object for each tag
	 * decoded from the file is placed in the object array in the order they
	 * were read from the file. If an error occurs while reading and parsing the
	 * file then an exception is thrown.
	 * 
	 * @param path
	 *            the path to the Flash Video file that will be parsed.
	 * 
	 * @throws FileNotFoundException -
	 *             if an error occurs while reading the file.
	 * @throws DataFormatException -
	 *             if the file does not contain Flash data.
	 * @throws IOException -
	 *             if an error occurs while reading and decoding the file.
	 */
	public void decodeFromFile(String path) throws IOException, DataFormatException
	{
		decodeFromFile(new File(path));
	}

	/**
	 * Decodes the contents of the specified file. An object for each tag
	 * decoded from the file is placed in the object array in the order they
	 * were read from the file. If an error occurs while reading and parsing the
	 * file then an exception is thrown.
	 * 
	 * @param file
	 *            the Flash Video file that will be parsed.
	 * 
	 * @throws FileNotFoundException -
	 *             if an error occurs while reading the file.
	 * @throws DataFormatException -
	 *             if the file does not contain Flash data.
	 * @throws IOException -
	 *             if an error occurs while reading and decoding the file.
	 */
	public void decodeFromFile(File file) throws IOException, DataFormatException
	{
		FileInputStream fileContents = new FileInputStream(file);

		int fileLength = (int) file.length();
		byte[] contents = new byte[fileLength];

		fileContents.read(contents);
		fileContents.close();

		decodeFromData(contents);
	}

	/**
	 * Decodes the binary Flash Video data stored in the byte array. If an error
	 * occurs while the data is being decoded an exception is thrown.
	 * 
	 * @param bytes
	 *            an array of bytes that contain the encoded Flash Video
	 *            objects.
	 * 
	 * @throws DataFormatException -
	 *             if the file does not contain Flash data.
	 * @throws IOException -
	 *             if an error occurs while reading and decoding the file.
	 */
	public void decodeFromData(byte[] bytes) throws IOException, DataFormatException
					
	{
		ByteBuffer coder = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);

		isFlashVideo(bytes);

		byte[] data = new byte[3];
		coder.get(data);
		
		signature = new String(data, "UTF8");
		version = coder.get();
		coder.get(); // audio & video flags		
		coder.getInt(); // header length always 9
		coder.getInt(); // previous length

		objects = new ArrayList<VideoTag>();
		
		int type;
		
		do
		{
			type = coder.get();
			coder.position(coder.position()-1);
			
			switch (type)
			{
				case VideoTypes.AUDIO_DATA:
					objects.add(new AudioData(coder));
					break;
				case VideoTypes.VIDEO_DATA:
					objects.add(new VideoData(coder));
					break;
				case VideoTypes.META_DATA:
					objects.add(new VideoMetaData(coder));
					break;
				default:
					break;//TODO(code) fix
			}
			coder.getInt(); // previous length

		} while (coder.position() < coder.limit());
	}

	/**
	 * Encodes the array of objects and writes the data to the specified file.
	 * If an error occurs while encoding the file then an exception is thrown.
	 * 
	 * @param path
	 *            the path to the file that the video will be encoded to.
	 * 
	 * @throws FileNotFoundException -
	 *             if an error occurs while reading the file.
	 * @throws IOException -
	 *             if an error occurs while encoding and writing the file.
	 */
	public void encodeToFile(String path) throws IOException
	{
		FileOutputStream fileContents = new FileOutputStream(path);

		byte[] encodedData = encode();

		fileContents.write(encodedData);
		fileContents.close();
	}

	/**
	 * Encodes the array of objects and writes the data to the specified file.
	 * If an error occurs while encoding the file then an exception is thrown.
	 * 
	 * @param file
	 *            the file that the video will be encoded to.
	 * 
	 * @throws FileNotFoundException -
	 *             if an error occurs while reading the file.
	 * @throws IOException -
	 *             if an error occurs while encoding and writing the file.
	 */
	public void encodeToFile(File file) throws IOException
	{
		FileOutputStream fileContents = new FileOutputStream(file);

		byte[] encodedData = encode();

		fileContents.write(encodedData);
		fileContents.close();
	}

	/**
	 * Returns the encoded representation of the array of objects that this
	 * Video contains. If an error occurs while encoding the file then an
	 * exception is thrown.
	 * 
	 * @return the array of bytes representing the encoded objects.
	 * 
	 * @throws IOException
	 *             if an error occurs while the data is being decoded.
	 */
	public byte[] encode() throws IOException
	{
		int fileLength = prepareToEncode();

		ByteBuffer coder = ByteBuffer.allocate(fileLength);
		
		int flags = 0;

		for (VideoTag object : objects)
		{
			if (object instanceof AudioData) {
				flags |= 4;
			}
			else if (object instanceof VideoData) {
				flags |= 1;
			}
		}

		coder.put(signature.getBytes("UTF8"));
		coder.put((byte)version);
		coder.put((byte)flags);
		coder.putInt(9);
		coder.putInt(0);

		for (VideoTag object : objects) {
			object.encode(coder);
		}
		return coder.array();
	}

	/**
	 * Creates and returns a deep copy of this object.
	 */
	public Video copy() 
	{
		return  new Video(this);
	}

	@Override
	public String toString()
	{
		return String.format(FORMAT, signature, version, objects);
	}

	private int prepareToEncode()
	{
		int length = 13;

		for (VideoTag object : objects) {
			length += 4 + object.prepareToEncode();
		}
		return length;
	}

	private void isFlashVideo(byte[] bytes) throws DataFormatException
	{
		if (bytes == null || bytes.length < 8) {
			throw new DataFormatException(Strings.DATA_CANNOT_BE_NULL);
		}

		if (bytes[0] != 0x46 || bytes[1] != 0x4C || bytes[2] != 0x56) {
			throw new DataFormatException(Strings.INVALID_FLASH_VIDEO_SIGNATURE);
		}
	}
}