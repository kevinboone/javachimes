#!/bin/bash

# Outline installer for JavaChimes for Linux. There's no need to install --
#  you can just run "java -jar /path/to/javachimes-x.x.jar" from the 
#  command line. 
# If you do want to install, run this script from the main source directory, 
#  e.g., "sudo ./samples/install_linux.sh"

#DESTDIR=/
BINDIR=/usr/bin
SHAREDIR=/usr/share
MANDIR=/usr/share/man
MYSHAREDIR=/usr/share/javachimes
VERSION=1.0b

if [ -f pom.xml ]; then

  mkdir -p $DESTDIR/$BINDIR 
  mkdir -p $DESTDIR/$SHAREDIR
  mkdir -p $DESTDIR/$MYSHAREDIR
  mkdir -p $DESTDIR/$MANDIR/man1
  cp binaries/*jar $DESTDIR/$MYSHAREDIR 
  cat << EOF > $DESTDIR/$BINDIR/javachimes
  #!/bin/bash
  exec java \$JAVA_OPTS -jar $MYSHAREDIR/javachimes-$VERSION.jar "\$@"
EOF

  chmod 755 $DESTDIR/$BINDIR/javachimes
  cp man1/* $DESTDIR/$MANDIR/man1/
else

  echo Run this from the source directory, e.g., 
  echo \"sudo ./samples/install_linux.sh\"

fi

