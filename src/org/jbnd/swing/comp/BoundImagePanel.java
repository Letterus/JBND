package org.jbnd.swing.comp;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.support.ValueConverter;


/**
 * A specialized <tt>JLabel</tt> subclass used to bind images from
 * <tt>DataObject</tt>s to the user interface. Also facilitates setting images
 * (from files other <tt>Image</tt> references) on the <tt>DataObject</tt>,
 * through standard JBND value editing provided by <tt>Binding</tt>s.
 * <p>
 * The image data is expected to be found in <tt>DataObject</tt>s in the form of
 * a <tt>byte</tt> array, or another form that can be converted to a
 * <tt>byte</tt> array using JBND's <tt>ValueConverter</tt> The data itself
 * should be an encoded image that can be decoded by the currently running JVM.
 * <p>
 * <b>Dimensions:</b> The <tt>BoundImagePanel</tt> operates by accepting a set
 * of dimensions that represent the maximum allowed size of the image, as well
 * as a scale parameter that determines what should be the scale of the image
 * when displayed in the user interface. To reiterate, the first set of
 * dimensions defines the maximum size of an image to be stored in the
 * <tt>DataObject</tt>s (if larger images are provided they will be
 * automatically scaled down), and the scale defines how they should be scaled
 * for the user interface.
 * <p>
 * More options specific to the <tt>BoundImagePanel</tt> are available, see the
 * provided methods. Besides that, and some automations of the size of the
 * component itself, the <tt>BoundImagePanel</tt> behaves like a standard
 * <tt>JLabel</tt>, it can contain text, even be bound to some other text
 * property of a <tt>DataObject</tt>.
 * 
 * @version 1.0 Mar 2, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see ValueConverter
 */
public class BoundImagePanel extends JLabel implements Bound, MouseListener{
	
	//	the Binding this bound is connected to
	private Binding binding;
	
	//	the image file, could be null
	private File imageFile;
	
	//	the icon being displayed when there is nothing else
	private ImageIcon defaultIcon;
	
	//	the dimension of the image being managed
	private Dimension dimension;
	
	//	the dimension of the image being displayed
	private Dimension displayDimension;
	
	//	the encoding format name, determines which kind of
	//	an image encoder will be used when storing image data
	private String encodingFormatName = "jpeg";
	
	//	a cursor used when the user is hovering over the component
	private Cursor	hoverCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	
	/**
	 * No-arg constructor, Bean conformability.
	 */
	public BoundImagePanel(){
		addMouseListener(this);
	}
	
	/**
	 * Creates a new <tt>BoundImagePanel</tt> with the given parameters.
	 * 
	 * @param imageDimension The maximum size of the images managed by this
	 *            <tt>BoundImagePanel</tt>. In combination with the
	 *            <tt>scale</tt> parameter this dimension will determine the
	 *            size of the panel.
	 * @param displayScale The ratio between the maximum size of stored images, and the
	 *            size of this <tt>BoundImagePanel</tt> in the user interface.
	 *            The width and height of the UI component are obtained by
	 *            multiplying the width and height of the given
	 *            <tt>dimension</tt>.
	 */
	public BoundImagePanel(Dimension imageDimension, float displayScale){
		this();
		setDimensions(imageDimension, displayScale);
	}
	
	/**
	 * Creates a new <tt>BoundImagePanel</tt> with the given parameters.
	 * 
	 * @param binding The <tt>Binding</tt> to whose first <tt>DataObject</tt>'s
	 *            first key the image is bound to.
	 * @param imageDimension The maximum size of the images managed by this
	 *            <tt>BoundImagePanel</tt>. In combination with the
	 *            <tt>scale</tt> parameter this dimension will determine the
	 *            size of the panel.
	 * @param displayScale The ratio between the maximum size of stored images, and the
	 *            size of this <tt>BoundImagePanel</tt> in the user interface.
	 *            The width and height of the UI component are obtained by
	 *            multiplying the width and height of the given
	 *            <tt>dimension</tt>.
	 */
	public BoundImagePanel(Binding binding, Dimension imageDimension, float displayScale){
		this(imageDimension, displayScale);
		setBinding(binding);
	}
	
	/**
	 * Sets the maximum dimensions of the images managed by this
	 * <tt>BoundImagePanel</tt>, which will in combination with the
	 * <tt>scale</tt> parameter update the dimensions of the panel itself.
	 * 
	 * @param imageDimension The maximum size of the images managed by this
	 *            <tt>BoundImagePanel</tt>. In combination with the
	 *            <tt>scale</tt> parameter this dimension will determine the
	 *            size of the panel.
	 * @param displayScale The ratio between the maximum size of stored images, and the
	 *            size of this <tt>BoundImagePanel</tt> in the user interface.
	 *            The width and height of the UI component are obtained by
	 *            multiplying the width and height of the given
	 *            <tt>dimension</tt>.
	 */
	public void setDimensions(Dimension imageDimension, float displayScale){
		//	set the dimension variables
		this.dimension = imageDimension;
		displayDimension = new Dimension((int)(imageDimension.width*displayScale),
				(int)(imageDimension.height*displayScale));
		
		//	set the size properties
		setPreferredSize(displayDimension);
		setMinimumSize(displayDimension);
		setMaximumSize(displayDimension);
		
		//	deal with the default icon
		if(defaultIcon == null){
			BufferedImage buffedImage = new BufferedImage(
				displayDimension.width, displayDimension.height, BufferedImage.TYPE_INT_ARGB);
			defaultIcon = new ImageIcon(buffedImage);
		}
	}
	
	/**
	 * Sets the image on the <tt>BoundImagePanel</tt>, this will result in
	 * the image being displayed by the panel and passed on to the
	 * <tt>Binding</tt>, to be stored in the bound <tt>DataObject</tt>.
	 * 
	 * @param imageFile A file containing an image.
	 */
	public void setImage(File imageFile){
		setImage(Toolkit.getDefaultToolkit().createImage(imageFile.getAbsolutePath()));
		this.imageFile = imageFile;
	}
	
	/**
	 * Sets the image on the <tt>BoundImagePanel</tt>, this will result in
	 * the image being displayed by the panel and passed on to the
	 * <tt>Binding</tt>, to be stored in the bound <tt>DataObject</tt>.
	 * 
	 * @param image An image.
	 */
	public void setImage(Image image){
		//	set the image on the data object, if possible
		if(binding.size() == 0) return;
		
		//	reset the image file
		this.imageFile = null;
		
		//	a byte array where the image data will be generated into
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try{
			//	write image into byte data
			RenderedImage scaledImage = toRendered(scale(image, dimension));
			boolean succesful = ImageIO.write(scaledImage, 
					encodingFormatName, new BufferedOutputStream(outputStream));
			if(!succesful)
				throw new Exception("Failed to find image writer for format name: "+
						encodingFormatName);
		}catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
		//	set the byte data on the data object
		//	through the binding
		byte[] imageData = outputStream.toByteArray();
		binding.edit(0, binding.getKeys()[0], imageData);
	}

	/**
	 * Returns the current <tt>Binding</tt> of this <tt>BoundImagePanel</tt>.
	 * 
	 * @return	The current <tt>Binding</tt> of this <tt>BoundImagePanel</tt>.
	 */
	public Binding getBinding(){
		return binding;
	}
	
	/**
	 * Sets the <tt>Binding</tt> for this <tt>BoundImagePanel</tt>.
	 * 
	 * @param binding	The new <tt>Binding</tt> for this <tt>BoundImagePanel</tt>.
	 */
	public void setBinding(Binding binding){
		if(this.binding == binding) return;
		if(this.binding != null)
			this.binding.removeDataSourceListener(this);
		
		this.binding = binding;
		binding.addDataSourceListener(this);
		updateImageFromBinding();
	}
	
	/**
	 * Gets the encoding format name of the encoder that is used to encode
	 * images when they are stored into <tt>DataObject</tt>s. The standard
	 * used is the one of the <tt>ImageIO.write(RenderedImage, 
	 * encodingName, OutputStream)</tt>
	 * method, the given <tt>encodingFormatName</tt> will be passed along to
	 * that method. A valid example is 'jpeg' or 'tiff'...
	 * 
	 * @return See above.
	 */
	public String getEncodingFormatName(){
		return encodingFormatName;
	}

	/**
	 * Sets the encoding format name of the encoder that should be used to
	 * encode images when they are stored into <tt>DataObject</tt>s. The
	 * standard used is the one of the <tt>ImageIO.write(RenderedImage, 
	 * encodingName, OutputStream)</tt>
	 * method, the given <tt>encodingFormatName</tt> will be passed along to
	 * that method. A valid example is 'jpeg' or 'tiff'...
	 * 
	 * @param encodingFormatName See above.
	 */
	public void setEncodingFormatName(String encodingFormatName){
		this.encodingFormatName = encodingFormatName;
	}

	/**
	 * Sets the cursor that should be used for the hovering behavior (when the
	 * cursor is visually above this <tt>BoundImagePanel</tt>). If
	 * <code>null</code>, the default cursor will be used.
	 * 
	 * @param hoverCursor The cursor to be used when the cursor is above the
	 *            component.
	 */
	public void setHoverCursor(Cursor hoverCursor){
		if(hoverCursor == null) hoverCursor = Cursor.getDefaultCursor();
		this.hoverCursor = hoverCursor;
	}

	/**
	 * If the last modification of the image of this <tt>BoundImagePanel</tt>
	 * was done through the <tt>setImage(File)</tt> method, then this method
	 * will return the <tt>File</tt> reference that was passed, otherwise it
	 * will return null.
	 * 
	 * @return See above.
	 */
	public File getImageFile(){
		return imageFile;
	}
	
	/**
	 * Gets the icon that is displayed in this <tt>BoundImagePanel</tt>
	 * when there is no image obtained through the <tt>Binding</tt>,
	 * like a placeholder image.
	 * 
	 * @return	See above.
	 */
	public ImageIcon getDefaultIcon(){
		return defaultIcon;
	}

	/**
	 * Sets the icon that is displayed in this <tt>BoundImagePanel</tt> when
	 * there is no image obtained through the <tt>Binding</tt>, like a
	 * placeholder image.
	 * 
	 * @param defaultIcon The icon to be used, if <code>null</code> nothing
	 *            will be displayed, simply an empty panel.
	 */
	public void setDefaultIcon(ImageIcon defaultIcon){
		if(defaultIcon == null) return;
		ImageIcon oldIcon = this.defaultIcon;
		this.defaultIcon = new ImageIcon(scale(defaultIcon.getImage(), displayDimension));
		if(getIcon() == oldIcon) setIcon(defaultIcon);
	}
	
	/*
	 * 
	 * 		DataSourceListener implementation
	 * 
	 */

	/**
	 * Responds to events fired by the <tt>Binding</tt>, if necessary updates
	 * the displayed image.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		if(event.getIndex0() == 0)
			updateImageFromBinding();
	}
	
	/**
	 * Responds to events fired by the <tt>Binding</tt>, if necessary updates
	 * the displayed image.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		if(event.getIndex0() == 0)
			updateImageFromBinding();
	}

	/**
	 * Responds to events fired by the <tt>Binding</tt>, if necessary updates
	 * the displayed image.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		if(event.getIndex0() == 0)
			updateImageFromBinding();
	}
	
	/*
	 * 
	 * 		MouseListener implementation
	 * 
	 */
	
	/**
	 * Can be overridden to for example open up a file dialog allowing the
	 * user to choose an image, and then calling <tt>setImage(File)</tt> on
	 * this <tt>BoundImagePanel</tt>. This implementation does nothing.
	 * 
	 * @param	e	Ignored.
	 */
	public void mouseClicked(MouseEvent e){}

	/**
	 * Sets the current hover cursor on the <tt>BoundImagePanel</tt>, in combination
	 * with the <tt>mouseExited(MouseEvent)</tt> method this provides a hover style
	 * cursor change behavior.
	 * 
	 * @param e	Ignored.
	 */
	public void mouseEntered(MouseEvent e){
		setCursor(hoverCursor);
	}

	/**
	 * Sets the default cursor on the <tt>BoundImagePanel</tt>, in combination
	 * with the <tt>mouseEntered(MouseEvent)</tt> method this provides a hover style
	 * cursor change behavior.
	 * 
	 * @param e	Ignored.
	 */
	public void mouseExited(MouseEvent e){
		setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * Does nothing.
	 * 
	 * @param e	Ignored.
	 */
	public void mousePressed(MouseEvent e){}

	/**
	 * Does nothing.
	 * 
	 * @param e	Ignored.
	 */
	public void mouseReleased(MouseEvent e){}
	
	
	/*
	 * 
	 * 		Utility methods.
	 * 
	 */
	
	/**
	 * Updates the displayed image to the one currently provided by the
	 * <tt>Binding</tt>, if available.
	 */
	private void updateImageFromBinding(){
		if(binding == null || binding.size() == 0){
			setIcon(defaultIcon);
			return;
		}
		
		byte[] imageData = (byte[])ValueConverter.toClass(
				binding.get(0).get(binding.getKeys()[0]), byte[].class);
		setIcon(imageData == null ? defaultIcon : 
			new ImageIcon(scale(Toolkit.getDefaultToolkit().createImage(imageData), 
					displayDimension)));
	}

	
	private Image scale(Image image, Dimension dimension){
		int oldHeight = image.getHeight(null);
		while(oldHeight == -1) oldHeight = image.getHeight(null);
		int oldWidth = image.getWidth(null);
		while(oldWidth == -1) oldWidth = image.getWidth(null);
		int height = dimension.height;
		int width = dimension.width;
		
		if(oldHeight <= height && oldWidth <= width)
			return image;
		
		double ratioWidth = (double)oldWidth / width;
		double ratioHeight = (double)oldHeight / height;
		
		//	take the bigger ratio, as it becomes a divider
		double ratio = ratioWidth > ratioHeight ? ratioWidth : ratioHeight;
		
		//	calculate new dimensions
		int newWidth = (int)(oldWidth / ratio);
		int newHeight = (int)(oldHeight / ratio);
		
		//	can go off due to precision lost, so make sure the image is no more then max
		newWidth = newWidth > width ? width : newWidth;
		newHeight = newHeight > height ? height : newHeight;
		
		return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
	}
	
	private MediaTracker mediaTracker = new MediaTracker(this);
	
	private RenderedImage toRendered(Image image){
		if(image instanceof RenderedImage) return (RenderedImage)image;
		mediaTracker.addImage(image, 0);
		
		int width = image.getWidth(null);
		while(width == -1) width = image.getWidth(null);
		int height = image.getHeight(null);
		while(height == -1) height = image.getHeight(null);
		
		BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D)buffered.createGraphics();
		
		try{
			mediaTracker.waitForID(0);
			g2.drawImage(image, 0, 0, null);
			mediaTracker.removeImage(image);
		}catch(InterruptedException ex){ throw new RuntimeException(ex);}
			
		g2.dispose();
		
		return buffered;
	}
}