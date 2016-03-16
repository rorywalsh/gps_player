<CsoundSynthesizer>
<CsInstruments>
; Initialize the global variables. 
sr = 44100
ksmps = 32
nchnls = 2
0dbfs = 1

gkAmp init 0
;instrument will be triggered by keyboard widget
instr 1
	kPosition chnget "PositionPoint"
	kPositionChanged changed kPosition
	kRamp init 0
	if kPositionChanged == 1 then
		if kPositionChanged==1 then 
			event "i", 2, 0, 1, 1
		else
			event "i", 2, 0, 1, 0
		endif
	endif

	kMax init 1
	kMin init 0

	kFreq1 oscil 1, .5, 2 
	aOut1 oscili 1*gkAmp, 150*kFreq1, 99
	kFreq2 oscil 1, .5, 1 
	aOut2 oscili 1, 100*kFreq2, 99
	outs (aOut1+aOut2)/2, (aOut1+aOut2)/2



	printk 1, 1
endin

instr 2
	if p4==1 && gkAmp<=1 then
	gkAmp = gkAmp+.01
	elseif p4==0 && gkAmp>=0 then
	gkAmp = gkAmp-.01
	else
	turnoff 
	endif
endin

</CsInstruments>
<CsScore>
;causes Csound to run for about 7000 years...
f99 0 8 10 1 .5 .2
f1 0 8 -2 0 1 2 0 1 2 4 3
f2 0 8 -2 3 2 1 0 3 2 0 1
;i1 0 36000
i1 1 36000

;i2 3 1 1
;i2 6 1 0
</CsScore>

</CsoundSynthesizer>
