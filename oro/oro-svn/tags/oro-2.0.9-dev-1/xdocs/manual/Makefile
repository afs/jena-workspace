#
# $Id: Makefile 54356 2001-07-09 11:55:12Z dfs $
#
# This Makefile requires the installation of at least OpenJade 1.3 and
# Docbook-Utils 0.6.  Output types other than html and rtf may require
# additional software such as jadetex and LaTeX.  Text output requires
# the lynx web browser.
#
# This Makefile is an interim measure that will go away and be replaced
# with Ant rules that are a part of the regular buid procedure before a
# formal release.
#

LIB_SGML_DIR = .
OUTPUT_DIR   = ../../docs/manual

DSSSL     = $(LIB_SGML_DIR)/oro.dsl
JADE      = openjade
JW        = jw -f docbook
LYNX      = lynx

ARTICLES  := $(basename $(shell find . -name "*.xml" -maxdepth 1 -print))

DVI_OBJ   := $(ARTICLES:%=%.dvi)
PS_OBJ    := $(ARTICLES:%=%.ps)
PDF_OBJ   := $(ARTICLES:%=%.pdf)
RTF_OBJ   := $(ARTICLES:%=%.rtf)
TXT_OBJ   := $(ARTICLES:%=%.txt)
HTML_OBJ  := $(ARTICLES:%=%.html)

OBJ = $(DVI_OBJ) $(PS_OBJ) $(PDF_OBJ) $(RTF_OBJ) $(TXT_OBJ) $(HTML_OBJ)

.SUFFIXES:
.SUFFIXES: .dvi .ps .pdf .rtf .txt .html .xml

%.dvi: %.xml .%.ts $(DSSSL)
	$(JW) -b dvi -d $(DSSSL)\#print $<

%.ps: %.xml .%.ts $(DSSSL)
	$(JW) -b ps -d $(DSSSL)\#print $<

%.pdf: %.xml .%.ts $(DSSSL)
	$(JW) -b pdf -d $(DSSSL)\#print $<

%.rtf: %.xml .%.ts $(DSSSL)
	$(JW) -b rtf -d $(DSSSL)\#print $<

%.html: %.xml .%.ts $(DSSSL)
	rm -f $@
	$(JADE) -V nochunks -t xml -i html -d $(DSSSL)\#html $< > $@

%.txt: %.html .%.ts $(DSSSL)
	rm -f $@
	$(LYNX) -force_html -dump -width=80 -nolist $< > $@

.guide.ts: guide.xml guide/*.xml
	touch $@

all: $(OBJ) copy

dvi: $(DVI_OBJ)

ps: $(PS_OBJ)

pdf: $(PDF_OBJ)

rtf: $(RTF_OBJ)

txt: $(TXT_OBJ)

html: $(HTML_OBJ)

copy:
	-mkdir $(OUTPUT_DIR) > /dev/null 2>&1 /dev/null
	-cp -f $(OBJ) $(OUTPUT_DIR) > /dev/null 2>&1

clean:
	/bin/rm -fr $(OBJ) .*.ts
