using System;

namespace StormingEditor
{
    public static class EventBus
    {
        public static event Action<string> OnEvent;

        public static void Trigger(string eventName)
        {
            OnEvent?.Invoke(eventName);
        }
    }
}