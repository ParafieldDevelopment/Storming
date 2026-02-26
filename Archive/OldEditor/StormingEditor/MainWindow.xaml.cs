public partial class MainWindow : Window
{
    private EditorApp _editorApp;

    public MainWindow()
    {
        InitializeComponent();

        _editorApp = new EditorApp();
        _editorApp.Run();

        // Optional: initialize panels
        SceneViewPanel.Content = new SceneViewPanel();
        HierarchyPanel.Content = new HierarchyPanel();
        InspectorPanel.Content = new InspectorPanel();
        ConsolePanel.Content = new ConsolePanel();
    }
}