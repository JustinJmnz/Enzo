import speech_recognition as sr
from gtts import gTTS
import os
import sys
from bluetooth import *
import threading


clientSocket = None
serverSocket = None

def sendBluetooth(message):
        clientSocket.send(message)

def receiveBluetooth():
        try:
            while True:
                data = clientSocket.recv(1024)
                if (len(data) == 0):
                        break
                else:
                        print("received [%s]" % data)
        except IOError:
            pass

        print("disconnected")

        clientSocket.close()
        serverSocket.close()
        
def bluetoothSetup():
        server_sock=BluetoothSocket( RFCOMM )
        server_sock.bind(("",PORT_ANY))
        server_sock.listen(1)

        port = server_sock.getsockname()[1]

        uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

        advertise_service( server_sock, "enzo",
                           service_id = uuid,
                           service_classes = [ uuid, SERIAL_PORT_CLASS ],
                           profiles = [ SERIAL_PORT_PROFILE ], 
                           #protocols = [ OBEX_UUID ] 
                            )
                           
        print("Waiting for connection on RFCOMM channel %d" % port)

        client_sock, client_info = server_sock.accept()
        print("Accepted connection from ", client_info)

        # These are global variables
        global clientSocket
        clientSocket = client_sock
        global serverSocket
        serverSocket = server_sock
        
        #sendBluetooth(clientSocket, "call Enea")
        #receiveBluetooth(clientSocket, serverSocket)
        
        print("all done")


        
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
                repeater("Calling: " + splitAudio[1])
                sendBluetooth("call " + splitAudio[1])
                return "Calling: " + splitAudio[1]
        elif(splitAudio[0] == "text"):
                repeater("What is your message?")
                message = listener()
                repeater("Texing" + splitAudio[1] + " saying " + message)
                sendBluetooth("text " + splitAudio[1] + " " + message)
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
         repeater("Listening")
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
if __name__ == "__main__":
        waiting = False
        bluetoothSetup()
        what_i_said = listener()
        if(what_i_said == None):
                sys.exit()
        print(what_i_said)
        command = parseAudio(what_i_said)
        print(command)
        
