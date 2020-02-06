#ifndef NMAINWINDOW_H
#define NMAINWINDOW_H

#include <QMainWindow>
#include <QFrame>
#include <QHBoxLayout>
#include <QGraphicsItem>
#include <QKeyEvent>

class NodeScene;
class NodeView;

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow() override;

private slots:
    void filterPressed();
    void autoLayout();
    void updateGridPic();
    void updateSnapToGrid();
    void onActionQuitTriggered();

private:
    void createStyle();
    void createMenu();
    void createView();
    void createBotMenu();
    void createCheckbox();

    NodeScene *scene;
    NodeView *view;
    QFrame *frame;
    QVBoxLayout *layout;
    QPixmap grid;
    bool gridPic;
};

#endif // NMAINWINDOW_H
