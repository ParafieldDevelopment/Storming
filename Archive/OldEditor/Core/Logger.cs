using System;

namespace StormingEditor

{
    public static class Logger
    {
        public static void Info(string message) => Console.WriteLine("[INFO] " + message);
        public static void Error(string message) => Console.WriteLine("[ERROR] " + message);
    }
}