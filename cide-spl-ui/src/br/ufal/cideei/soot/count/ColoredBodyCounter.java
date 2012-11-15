/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

//#ifdef METRICS
//@package br.ufal.cideei.soot.count;
//@
//@import java.util.Map;
//@
//@import soot.Body;
//@import br.ufal.cideei.soot.instrument.ConfigTag;
//@
//@public class ColoredBodyCounter extends BodyCounter {
//@
//@	private long coloredCounter = 0;
//@
//@	public Long getColoredCount() {
//@		return coloredCounter;
//@	}
//@
//@	@Override
//@	protected void internalTransform(Body body, String phase, Map opt) {
//@		super.internalTransform(body, phase, opt);
//@		ConfigTag tag = (ConfigTag) body.getTag(ConfigTag.CONFIG_TAG_NAME);
//@		//XXX check size consistency. Depends on instrumentation.
//@		if (tag.size() > 1)
//@			coloredCounter++;
//@	}
//@}
// #endif
