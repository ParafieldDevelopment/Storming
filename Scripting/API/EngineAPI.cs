namespace StormingEditor
{
    public static class EngineAPI
    {
        public static void Log(string message)
        {
            Logger.Info("[Script] " + message);
        }
    }
}