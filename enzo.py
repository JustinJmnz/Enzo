import speech_recognition as sr
from gtts import gTTS
import os
  
def repeater(string_to_repeat):
	print("Repeating: " + string_to_repeat)
	string_to_repeat = string_to_repeat + "."
	tts = gTTS(text=(string_to_repeat),lang="en")
	tts.save("output.mp3")
	##os.system("aplay -f dat output.mp3")
	os.system("mplayer output.mp3")
  
def listener():
     r = sr.Recognizer()
     with sr.Microphone(sample_rate = 48000) as source:
         audio = r.adjust_for_ambient_noise(source)
         print("Say something!")
         audio = r.listen(source)
  
         try:
              print("processing")
              what_i_said = r.recognize_google(audio)
              print("Google Speech Recognition " + what_i_said)
              repeater(what_i_said)
         except sr.UnknownValueError:
              print("Google Speech Recognition could not understand your audio")
         except sr.RequestError as e:
              print("Could not request results from Google Speech Recognition service; {0}".format(e))
  
listener()
