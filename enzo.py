import speech_recognition as sr
from gtts import gTTS
import os
import sys

# Repeats a given string to the user through usb speaker
def repeater(string_to_repeat):
	string_to_repeat = string_to_repeat + "."
	tts = gTTS(text=(string_to_repeat),lang="en")
	tts.save("output.mp3")
	os.system("mplayer output.mp3")

# Splits string into words
# if word[0] == call -> call
# if word[0] == text -> getMessage() -> text

def parseAudio(audio):
	splitAudio = audio.split()
	print("splitAudio[0]: " + splitAudio[0])
	print("splitAudio[1]: " + splitAudio[1])
	if(splitAudio[0] == "call"):
		return "Calling: " + splitAudio[1]
	elif(splitAudio[0] == "text"):
		repeater("What is your message?")
		message = listener()
		return "Texting: " + splitAudio[1] + " Message: " + message
	else:
		return audio

# Listens for speech
# Calls Google API to detect words
# Returns Google's Interpretation as String
def listener():
	 # Listens for a voice
     r = sr.Recognizer()
     with sr.Microphone(sample_rate = 48000) as source:
         audio = r.adjust_for_ambient_noise(source)
         print("Say something!")
         audio = r.listen(source)
  
         try:
              print("processing")
              what_i_said = r.recognize_google(audio)
              return what_i_said
         except sr.UnknownValueError:
              print("Google Speech Recognition could not understand your audio")
              return None
         except sr.RequestError as e:
              print("Could not request results from Google Speech Recognition service; {0}".format(e))
              return None
  
# Start of program
if __name__ == "main":
	what_i_said = listener()
	print(what_i_said)
	if(what_i_said == None):
		sys.exit()
	command = parseAudio(what_i_said)
	print(command)
