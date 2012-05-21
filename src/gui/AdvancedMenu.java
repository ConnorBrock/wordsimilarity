package gui;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import relations.WordRelator;
import tools.FlickrImage;
import tools.GoogleImage;
import tools.WeightedObject;

/**
 * Make menus.
 * @author bkievitk
 */

public class AdvancedMenu {
	
	/**
	 * Replace current extension with another.
	 * @param f
	 * @param newExtension
	 * @return
	 */
	public static File replaceFileExtension(File f, String newExtension) {
		
		// Find current extension.
		String path = f.getAbsolutePath();
		int sep = path.lastIndexOf(File.separatorChar);
		int dot = path.lastIndexOf('.');
		
		if(dot > sep) {
			return new File(path.substring(0,dot) + "." + newExtension);
		} else {
			return new File(path + "." + newExtension);
		}
	}
		
	/**
	 * Build the main menu bar.
	 * @return
	 */
	public static JMenuBar buildMenu(final MainGUI main) {				
		// Menu bar.
		JMenuBar menu = new JMenuBar();
		
		// Add each sub-menu.
		menu.add(buildFileMenu(main.wordMap, main));
		menu.add(buildToolsMenu(main));
		menu.add(buildRenderMenu(main));
		menu.add(buildLayoutMenu(main.wordMap, main));
		menu.add(buildConnectionMenu(main));	
		menu.add(buildHelpMenu(main));	
		return menu;
	}
	
	
	/**
	 * File controls IO.
	 * @param wordMap
	 * @return
	 */
	public static JMenu buildFileMenu(final WordMap wordMap, final MainGUI main) {

		JMenu file = new JMenu("File");
		
			// Simply clear all relations and words.
			JMenuItem newMenu = new JMenuItem("New");
				newMenu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						
						// Clear.
						wordMap.clear();
						
						// Show updates.
						wordMap.wordChanged();
						wordMap.relationChanged();
					}
				});
			file.add(newMenu);
						
			// Seperator.
			file.addSeparator();
			
			// Save worspace.
			JMenuItem save = new JMenuItem("Save");
				save.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						
						// File output types.	
						
						// Binary.
						javax.swing.filechooser.FileFilter full = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".w2w") || arg0.isDirectory();	}
							public String getDescription() {	return "Binary (.w2w)";	}							
						};

						// CSV Matrix.
						javax.swing.filechooser.FileFilter csv = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".csv") || arg0.isDirectory();	}
							public String getDescription() {	return "Comma Seperated Matrix File (.csv)";	}							
						};
						
						// CSV pair file.
						javax.swing.filechooser.FileFilter wordPairs = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".csv") || arg0.isDirectory();	}
							public String getDescription() {	return "Comma Seperated Word Pair File (.csv)";	}							
						};

						// CSV word list.
						javax.swing.filechooser.FileFilter wordList = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".wrd") || arg0.isDirectory();	}
							public String getDescription() {	return "Word List (.csv)";	}							
						};
						
						// compressed word list.
						javax.swing.filechooser.FileFilter compressedList = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".cmp") || arg0.isDirectory();	}
							public String getDescription() {	return "Compressed (.cmp)";	}							
						};

						// Create file chooser.
						final javax.swing.filechooser.FileFilter[] selectedFilter = new javax.swing.filechooser.FileFilter[1];
						JFileChooser chooser = new JFileChooser(new File("."));
						chooser.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent arg0) {
								if(arg0.getPropertyName() == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
									selectedFilter[0] = (javax.swing.filechooser.FileFilter)arg0.getNewValue();
								}
							}
						});

						// Add filters.
						chooser.setAcceptAllFileFilterUsed(false);		
						chooser.addChoosableFileFilter(full);		
						chooser.addChoosableFileFilter(csv);			
						chooser.addChoosableFileFilter(wordPairs);		
						chooser.addChoosableFileFilter(wordList);	
						chooser.addChoosableFileFilter(compressedList);

						// On complete.
						if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							try {
								File file = chooser.getSelectedFile();	

								// Check which filter was used.
								if(selectedFilter[0] == full) {
									file = replaceFileExtension(file,"w2w");
								} else if(selectedFilter[0] == csv) {
									file = replaceFileExtension(file,"csv");
								} else if(selectedFilter[0] == wordPairs) {
									file = replaceFileExtension(file,"csv");
								} else if(selectedFilter[0] == wordList) {
									file = replaceFileExtension(file,"wrd");
								} else if(selectedFilter[0] == compressedList) {
									file = replaceFileExtension(file,"cmp");
								}
																
								// Check if file already exists.
								if(file.exists()) {
									int n = JOptionPane.showConfirmDialog(
									    null,
									    "Overwrite file?",
									    "File already exists.",
									    JOptionPane.YES_NO_OPTION);
									
									if(n == JOptionPane.NO_OPTION) {
										System.out.println("Save canceled.");
										return;
									}
								}
								
								// Check which filter was used.
								if(selectedFilter[0] == full) {									
									// A full data dump.
									IO.saveWorkspace(wordMap, new FileOutputStream(file));									
								} else if(selectedFilter[0] == csv) {									
									// Blocks comparators into matricies.
									IO.saveCSV(wordMap, new FileOutputStream(file));								
								} else if(selectedFilter[0] == wordPairs) {
									// Blocks data into word pairs.
									// For stats.
									IO.savePairs(wordMap, new FileOutputStream(file));	
								} else if(selectedFilter[0] == wordList) {
									// List of all words.
									IO.saveWordList(wordMap, new FileOutputStream(file));									
								} else if(selectedFilter[0] == compressedList) {									
									// List of all words.
									IO.saveCMPFrame(wordMap, new FileOutputStream(file));
								}
								
							} catch (IOException e) {
								e.printStackTrace();
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
					}
				});
			file.add(save);
			
			// Load menu.
			JMenuItem load = new JMenuItem("Load");
				load.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent arg0) {
						
						javax.swing.filechooser.FileFilter full = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".w2w") || arg0.isDirectory();	}
							public String getDescription() {	return "Binary (.w2w)";	}							
						};

						javax.swing.filechooser.FileFilter csv = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".csv") || arg0.isDirectory();	}
							public String getDescription() {	return "Comma Seperated Matrix File (.csv)";	}							
						};

						javax.swing.filechooser.FileFilter images = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.isDirectory();	}
							public String getDescription() {	return "Image Directory (dir)";	}							
						};
						
						javax.swing.filechooser.FileFilter compressed = new javax.swing.filechooser.FileFilter() {
							public boolean accept(File arg0) {	return arg0.getName().endsWith(".cmp") || arg0.isDirectory();	}
							public String getDescription() {	return "Compressed (.cmp)";	}	
						};

						// Create file chooser.
						final javax.swing.filechooser.FileFilter[] selectedFilter = new javax.swing.filechooser.FileFilter[1];
						JFileChooser chooser = new JFileChooser(new File("."));
						chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						chooser.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent arg0) {
								if(arg0.getPropertyName() == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
									selectedFilter[0] = (javax.swing.filechooser.FileFilter)arg0.getNewValue();
								}
							}
						});

						// Add filters.
						chooser.setAcceptAllFileFilterUsed(false);		
						chooser.addChoosableFileFilter(full);		
						chooser.addChoosableFileFilter(csv);		
						chooser.addChoosableFileFilter(images);		
						chooser.addChoosableFileFilter(compressed);	

						// On complete.
						if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							try {
								File file = chooser.getSelectedFile();			
								if(selectedFilter[0] == full) {
									IO.loadWorkSpace(wordMap, new BufferedInputStream(new FileInputStream(file)));				
								} else if(selectedFilter[0] == csv) {
									IO.loadCSV(wordMap, new BufferedReader(new FileReader(file)));
								} else if(selectedFilter[0] == images) {
									IO.loadImages(wordMap, file);
									main.visualizationChanged();
								} else if(selectedFilter[0] == compressed) {
									IO.loadCMP(wordMap, new BufferedInputStream(new FileInputStream(file)));
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				});
			file.add(load);	
			
			JMenuItem loadWeb = new JMenuItem("Load Web");
			loadWeb.addActionListener(new ActionListener() {
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent arg0) {
					try {
						BufferedReader r = new BufferedReader(new InputStreamReader((new URL("http://www.indiana.edu/~semantic/word2word/listFiles.php")).openStream()));
						String line;
						
						Vector<WeightedObject<String>> sizes = new Vector<WeightedObject<String>>();
						while((line = r.readLine()) != null) {
							String[] parts =line.split(";");
							String file = parts[0];
							int size = Integer.parseInt(parts[1]);
							sizes.add(new WeightedObject<String>(file, size));
						}
						
						WeightedObject<String> s = (WeightedObject<String>)JOptionPane.showInputDialog(
						                    null,
						                    "Select file to download.",
						                    "Web Files",
						                    JOptionPane.PLAIN_MESSAGE,
						                    null,
						                    sizes.toArray(),
						                    "ham");

						//If a string was returned, say so.
						if(s != null) {
							IO.loadCMP(wordMap, new BufferedInputStream(new FileInputStream("IEP.cmp")));
						}
						
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			file.add(loadWeb);	
			

			file.addSeparator();
			
			// Take screenshot.
			JMenuItem screen = new JMenuItem("Screen Shot");
			screen.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					
					JFileChooser chooser = new JFileChooser(new File("."));
					chooser.showSaveDialog(null);
					File file = chooser.getSelectedFile();
					try {
						ImageIO.write(main.visualizationPanel.getImage(), "png", file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			file.add(screen);

			// Seperator.
			file.addSeparator();
			
			// Load images.
			JMenu images = new JMenu("Images");
			file.add(images);
			
			JMenuItem imagesGoogle = new JMenuItem("Google");
			imagesGoogle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//Custom button text
					Object[] options = {"No", "Yes"};
					int n = JOptionPane.showOptionDialog(null,
					    "This will load an image from Google for each active word, into a directory of your choosing.\nYou may then load the images for the nodes using File->Load->Image Directory.\nThis action may take a while. Are you sure you would like to continue?",
					    "Continue",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[0]);
					if(n == 1) {
						JFileChooser dirChooser = new JFileChooser(new File("."));
						dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if(dirChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							File f = dirChooser.getSelectedFile();
							for(String word : wordMap.activeWords.keySet()) {
								BufferedImage image = GoogleImage.retrieveImages(word, 1).get(0);
								try {
									ImageIO.write(image, "png", new File(f.getAbsolutePath() + File.separatorChar + word + ".png"));
								} catch(IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			});
			images.add(imagesGoogle);
			
			JMenuItem imagesFlickr = new JMenuItem("Flickr");
			imagesFlickr.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					//Custom button text
					Object[] options = {"No", "Yes"};
					int n = JOptionPane.showOptionDialog(null,
					    "This will load an image from Flickr for each active word, into a directory of your choosing.\nYou may then load the images for the nodes using File->Load->Image Directory.\nThis action may take a while. Are you sure you would like to continue?",
					    "Continue",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[0]);
					if(n == 1) {
						JFileChooser dirChooser = new JFileChooser(new File("."));
						dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if(dirChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							File f = dirChooser.getSelectedFile();
							for(String word : wordMap.activeWords.keySet()) {
								BufferedImage image = FlickrImage.retrieveImages(word, 1).get(0);
								try {
									ImageIO.write(image, "png", new File(f.getAbsolutePath() + File.separatorChar + word + ".png"));
								} catch(IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			});
			images.add(imagesFlickr);
			
		return file;
	}
	
	public static JMenu buildHelpMenu(final MainGUI main) {

		JMenu help = new JMenu("Help");
		
		JMenuItem tutorial = new JMenuItem("Tutorial");
		tutorial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.showTutorial();
			}
		});
		help.add(tutorial);
		
		return help;
	}
	
	/**
	 * Tools opens the various tool windows.
	 * @param main
	 * @return
	 */
	public static JMenu buildToolsMenu(final MainGUI main) {

		JMenu tools = new JMenu("Tools");
			
			JMenuItem wizard = new JMenuItem("Similarity Wizard");
			wizard.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					main.showWizardFrame();
				}
			});
			tools.add(wizard);
				
			JMenuItem wordManager = new JMenuItem("Word Manager");
			wordManager.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					main.showWordFrame();
				}
			});
			tools.add(wordManager);
						
			JMenuItem networkToolsMenu = new JMenuItem("Network Tools");
			networkToolsMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					main.showNetworkTools();
				}
			});
			tools.add(networkToolsMenu);
			
			JMenuItem showOptionsMenu = new JMenuItem("Show Options");
			showOptionsMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					main.showOptions();
				}
			});
			tools.add(showOptionsMenu);
			
		return tools;
	}
	
	/**
	 * Manage rendering capabilities.
	 * @param main
	 * @return
	 */
	public static JMenu buildRenderMenu(final MainGUI main) {
		JMenu render = new JMenu("Render");
		
		JMenuItem renderNow = new JMenuItem("Render Once");
		renderNow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.showRenderingProgress();					
				main.visualizationPanel.imageChanged = true;
				main.visualizationPanel.renderState = Visualization.STATE_RENDER_ONCE;
				main.visualizationChanged();
			}
		});
		render.add(renderNow);
		
		final JMenuItem renderAlwaysMenu = new JMenuItem("Render Always");
		renderAlwaysMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				main.visualizationPanel.imageChanged = true;
				if(main.visualizationPanel.renderState == Visualization.STATE_RENDER_SMART || main.visualizationPanel.renderState == Visualization.STATE_RENDER_ONCE) {
					main.visualizationPanel.renderState = Visualization.STATE_RENDER_ALWAYS;
					renderAlwaysMenu.setText("Render Smart");
				} else {
					main.visualizationPanel.renderState = Visualization.STATE_RENDER_SMART;
					renderAlwaysMenu.setText("Render Always");
				}
				main.visualizationChanged();
			}
		});
		render.add(renderAlwaysMenu);	
		
		return render;	
	}
	
	/**
	 * Select a default layout.
	 * @param wordMap
	 * @param main
	 * @return
	 */
	public static JMenu buildLayoutMenu(final WordMap wordMap, final MainGUI main) {
		
		JMenu layout = new JMenu("Layout");
		
		JMenuItem randomize = new JMenuItem("Random Layout");
		randomize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Layouts.layoutRandom(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		layout.add(randomize);

		JMenuItem grid = new JMenuItem("Grid Layout");
		grid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Layouts.layoutGrid(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		layout.add(grid);
		
		JMenuItem centered = new JMenuItem("Word Centered Layout");
		centered.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				WordRelator wordRelator = (WordRelator)JOptionPane.showInputDialog(
                    null,
                    "Select comparator.",
                    "Comparator",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    wordMap.activeRelations.toArray(),
                    null);
			
				String word = (String)JOptionPane.showInputDialog(
                    null,
                    "Select word.",
                    "Word",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);
					
				Layouts.layoutWordCentered(wordMap, main.visualizationPanel.getSize(),wordRelator,word);
				main.visualizationChanged();
			}
		});
		layout.add(centered);			

		JMenuItem mds = new JMenuItem("MDS Layout");
		mds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WordRelator wordRelator = (WordRelator)JOptionPane.showInputDialog(
                    null,
                    "Select comparator to use for MDS.",
                    "Comparator Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    wordMap.activeRelations.toArray(),
                    null);

				Layouts.layoutMDS(wordMap, main.visualizationPanel.getSize(),wordRelator, false);
				Layouts.layoutFitScreen(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		layout.add(mds);

		JMenuItem tsne = new JMenuItem("tSNE Layout");
		tsne.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WordRelator wordRelator = (WordRelator)JOptionPane.showInputDialog(
                    null,
                    "Select comparator to use for tSNE.",
                    "Comparator Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    wordMap.activeRelations.toArray(),
                    null);

				Layouts.layoutTSNE(wordMap, main.visualizationPanel.getSize(),wordRelator, false);
				Layouts.layoutFitScreen(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		layout.add(tsne);
		
		JMenuItem fit = new JMenuItem("Fit Screen");
		fit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {					
				Layouts.layoutFitScreen(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		layout.add(fit);
		
		
		

		JMenuItem binary = new JMenuItem("Sided Layout");
		binary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {		
				
				WordRelator wordRelator1 = (WordRelator)JOptionPane.showInputDialog(
	                    null,
	                    "Select first comparator.",
	                    "Comparator Selection",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    wordMap.activeRelations.toArray(),
	                    null);
				
				WordRelator wordRelator2 = (WordRelator)JOptionPane.showInputDialog(
	                    null,
	                    "Select second comparator.",
	                    "Comparator Selection",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    wordMap.activeRelations.toArray(),
	                    null);
				
				Layouts.layoutBinary(wordMap, wordRelator1, wordRelator2);
				Layouts.layoutFitScreen(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		layout.add(binary);
		
		
		
		
		
		JMenu threeD = new JMenu("3D");
		layout.add(threeD);
		
		tsne = new JMenuItem("tSNE Layout 3D");
		tsne.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WordRelator wordRelator = (WordRelator)JOptionPane.showInputDialog(
                    null,
                    "Select comparator to use for tSNE.",
                    "Comparator Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    wordMap.activeRelations.toArray(),
                    null);

				Layouts.layoutTSNE(wordMap, main.visualizationPanel.getSize(),wordRelator, true);
				Layouts.layoutFitScreen(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		threeD.add(tsne);

		mds = new JMenuItem("MDS Layout 3D");
		mds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WordRelator wordRelator = (WordRelator)JOptionPane.showInputDialog(
                    null,
                    "Select comparator to use for MDS.",
                    "Comparator Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    wordMap.activeRelations.toArray(),
                    null);

				Layouts.layoutMDS(wordMap, main.visualizationPanel.getSize(),wordRelator, true);
				Layouts.layoutFitScreen(wordMap, main.visualizationPanel.getSize());
				main.visualizationChanged();
			}
		});
		threeD.add(mds);
		
		centered = new JMenuItem("Word Centered Layout 3D");
		centered.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				WordRelator wordRelator = (WordRelator)JOptionPane.showInputDialog(
                    null,
                    "Select comparator.",
                    "Comparator",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    wordMap.activeRelations.toArray(),
                    null);
			
				String word = (String)JOptionPane.showInputDialog(
                    null,
                    "Select word.",
                    "Word",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);
					
				Layouts.layoutWordCentered3D(wordMap, main.visualizationPanel.getSize(),wordRelator,word);
				main.visualizationChanged();
			}
		});
		threeD.add(centered);			
		
		return layout;	
	}
	
	/**
	 * Select connection type.
	 * @param wordMap
	 * @param main
	 * @return
	 */
	public static JMenu buildConnectionMenu(final MainGUI main) {

		JMenu connectionType = new JMenu("Connection");
		
			JMenuItem connLine = new JMenuItem("Line");
			connLine.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					main.options.biDirectionalType = Options.BI_DIRECTIONAL_NONE;
					main.visualizationChanged();
				}			
			});
			connectionType.add(connLine);
		
			JMenuItem connArrow = new JMenuItem("Arrow");
			connArrow.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					main.options.biDirectionalType = Options.BI_DIRECTIONAL_ARROW;
					main.visualizationChanged();
				}			
			});
			connectionType.add(connArrow);
	
			JMenuItem connTopBottom = new JMenuItem("Top & Bottom");
			connTopBottom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					main.options.biDirectionalType = Options.BI_DIRECTIONAL_TOP_BOTTOM;
					main.visualizationChanged();
				}			
			});
			connectionType.add(connTopBottom);

			JMenuItem connArrowMid = new JMenuItem("Middle Arrow");
			connArrowMid.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					main.options.biDirectionalType = Options.BI_DIRECTIONAL_ARROW_MID;
					main.visualizationChanged();
				}			
			});
			connectionType.add(connArrowMid);
			
			JMenuItem connDotEnd = new JMenuItem("Dot End");
			connDotEnd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					main.options.biDirectionalType = Options.BI_DIRECTIONAL_DOT_END;
					main.visualizationChanged();
				}			
			});
			connectionType.add(connDotEnd);
		
			JMenuItem connCustom = new JMenuItem("Spike");
			connCustom.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					main.options.biDirectionalType = Options.BI_DIRECTIONAL_SPIKE;
					main.visualizationChanged();
				}			
			});
			connectionType.add(connCustom);

		return connectionType;			
	}
	
}
