/*
 * Truffautronic
 * Copyright (c) 2014 Laurent GRÉGOIRE <laurent.gregoire@gmail.com>
 * & Lycée Général et Technologique François Truffaut - Challans
 *
 * Truffautronic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Truffautronic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Truffautronic.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.truffautronic.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.truffautronic.model.AudioCue;
import org.truffautronic.model.AudioParams;
import org.truffautronic.model.ClipCueAudioFactory;
import org.truffautronic.model.CueAudioFactory;
import org.truffautronic.model.Duration;
import org.truffautronic.model.OutputMixer;
import org.truffautronic.model.Scenario;
import org.truffautronic.model.Scene;
import org.truffautronic.model.TimeLabel;
import org.truffautronic.model.TimeLabelsBundle;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ScenarioIO {

	private static class DurationConverter implements Converter {

		@SuppressWarnings("rawtypes")
		@Override
		public boolean canConvert(Class type) {
			return Duration.class.equals(type);
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			return new Duration(reader.getValue());
		}

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			Duration duration = (Duration) source;
			writer.setValue(duration.toString());
		}
	}

	private static class FileConverter implements Converter {

		private Path basePath;

		public FileConverter(File baseDir) {
			this.basePath = baseDir == null ? null : Paths.get(baseDir.toURI());
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean canConvert(Class type) {
			return File.class.equals(type);
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			String str = reader.getValue();
			File file = new File(str);
			if (!file.isAbsolute() && basePath != null) {
				// Convert to absolute file name, relative to baseDir
				file = new File(basePath.toFile().getAbsoluteFile()
						+ File.separator + file.getPath());
			}
			return file;
		}

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			File file = (File) source;
			String fileStr;
			if (file.isAbsolute() && basePath != null) {
				fileStr = basePath.relativize(file.toPath()).toString();
			} else {
				fileStr = file.toString();
			}
			writer.setValue(fileStr);
		}
	}

	private static class OutputMixerConverter implements Converter {

		@SuppressWarnings("rawtypes")
		@Override
		public boolean canConvert(Class type) {
			return OutputMixer.class.isAssignableFrom(type);
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			String mixerName = reader.getValue();
			Scenario scenario = (Scenario) context.get("scenario");
			return scenario.getOutputMixer(mixerName);
		}

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			OutputMixer outputMixer = (OutputMixer) source;
			writer.setValue(outputMixer.getName());
		}
	}

	private static class ScenarioConverter implements Converter {

		@SuppressWarnings("rawtypes")
		@Override
		public boolean canConvert(Class type) {
			return Scenario.class.equals(type);
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			Scenario scenario = new Scenario();
			context.put("scenario", scenario);
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				String nodeName = reader.getNodeName();
				if (nodeName.equals("scene")) {
					Scene scene = (Scene) context.convertAnother(scenario,
							Scene.class);
					scenario.addScene(scene);
				} else if (nodeName.equals("audioParams")) {
					AudioParams audioParams = (AudioParams) context
							.convertAnother(scenario, AudioParams.class);
					scenario.setAudioParams(audioParams);
				}
				reader.moveUp();
			}
			return scenario;
		}

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			Scenario scenario = (Scenario) source;
			writer.startNode("audioParams");
			context.convertAnother(scenario.getAudioParams());
			writer.endNode();
			for (Scene scene : scenario.getScenes()) {
				writer.startNode("scene");
				context.convertAnother(scene);
				writer.endNode();
			}
		}
	}

	public void save(Scenario scenario, File file) throws IOException {
		XStream xstream = createXStream(file.getAbsoluteFile().getParentFile());
		FileOutputStream fos = new FileOutputStream(file);
		xstream.toXML(scenario, fos);
		fos.close();
	}

	public byte[] checksum(Scenario scenario) {
		try {
			XStream xstream = createXStream(null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			xstream.toXML(scenario, baos);
			baos.close();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return md.digest(baos.toByteArray());
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Scenario load(File file) throws IOException {
		XStream xstream = createXStream(file.getAbsoluteFile().getParentFile());
		FileInputStream fis = new FileInputStream(file);
		Scenario scenario = (Scenario) xstream.fromXML(fis);
		fis.close();
		return scenario;
	}

	private XStream createXStream(File baseDir) {
		XStream xstream = new XStream();
		xstream.aliasSystemAttribute(null, "class");
		xstream.alias("scenario", Scenario.class);
		xstream.alias("audioParams", AudioParams.class);
		// xstream.addImplicitCollection(Scenario.class, "scenes");
		// xstream.addImplicitCollection(Scenario.class, "audioOutputs");
		xstream.alias("scene", Scene.class);
		xstream.addImplicitCollection(Scene.class, "cues");
		xstream.alias("audioCue", AudioCue.class);
		xstream.addDefaultImplementation(ClipCueAudioFactory.class,
				CueAudioFactory.class);
		xstream.aliasAttribute(AudioCue.class, "cueAudioFactory",
				"audioFactory");
		xstream.aliasAttribute(AudioCue.class, "volumeManager", "volume");
		xstream.alias("mixer", OutputMixer.class);
		xstream.alias("labels", TimeLabelsBundle.class);
		xstream.addImplicitCollection(TimeLabelsBundle.class, "timeLabels");
		xstream.alias("label", TimeLabel.class);
		xstream.registerConverter(new DurationConverter());
		xstream.registerConverter(new ScenarioConverter());
		xstream.registerConverter(new OutputMixerConverter());
		xstream.registerConverter(new FileConverter(baseDir));
		xstream.setMode(XStream.NO_REFERENCES);
		return xstream;
	}
}
