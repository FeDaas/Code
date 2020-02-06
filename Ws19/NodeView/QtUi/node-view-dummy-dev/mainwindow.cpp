#include "mainwindow.h"
#include "nodeview.h"
#include "nodescene.h"

#include <QMenuBar>
#include <QPushButton>
#include <QSize>
#include <QCheckBox>

MainWindow::MainWindow(QWidget *parent)
{   
    Q_UNUSED(parent)

    frame = new QFrame(this);
    frame->setMinimumSize(QSize(680, 460));
    setCentralWidget(frame);

    layout = new QVBoxLayout(frame);

    createMenu();
    createView();
    createBotMenu();

    createStyle();
}

MainWindow::~MainWindow()
{
    QList<QGraphicsItem *> items = scene->items();
    for (QGraphicsItem *item : qAsConst(items))
    {
        scene->removeItem(item);
        delete item;
    }

    delete scene;
    delete view;
    delete layout;
    delete frame;
}

void MainWindow::createStyle()
{
    QFile styleFile(":/styles/styles.qss");
    styleFile.open(QFile::ReadOnly);
    setStyleSheet(QString::fromLatin1(styleFile.readAll()));
    styleFile.close();
}

void MainWindow::createMenu()
{
    QMenu *menu = menuBar()->addMenu(tr("Menu"));
    QAction *quitAction = new QAction(tr("Quit"));
    menu->addAction(quitAction);

    menu = menuBar()->addMenu(tr("Help"));

    connect(quitAction, &QAction::triggered, this, &MainWindow::onActionQuitTriggered);
}

void MainWindow::createView()
{
    view = new NodeView();
    view->setFrameStyle(QFrame::Box | QFrame::Sunken);
    view->setLineWidth(1);
    layout->addWidget(view);

    scene = new NodeScene(frame);
    grid = QPixmap(":/images/grid.png");
    scene->setBackgroundBrush(grid);
    gridPic=true;
    scene->setSceneRect(0, 0, 5000, 5000);

    view->setScene(scene);
}

void MainWindow::createBotMenu()
{
    QMenuBar *botMenu = new QMenuBar();
    botMenu->setDefaultUp(true);
    layout->addWidget(botMenu);

    QMenu *filterMenu = botMenu->addMenu(tr("Filters"));
    for (int i = 1; i <= 3; ++i)
    {
        QString name = "Filter " + QString::number(i);
        QAction *filter = new QAction(name);
        filterMenu->addAction(filter);
        connect(filter, &QAction::triggered, this, &MainWindow::filterPressed);
    }

    QMenu *settingsMenu = botMenu->addMenu(tr("Settings"));
    {
        //GridPic
        QAction *setting = new QAction(tr("Grid"));
        setting->setCheckable(true);
        setting->setChecked(true);
        settingsMenu->addAction(setting);
        connect(setting, &QAction::changed, this, &MainWindow::updateGridPic);
        //GidSnap
        QAction *snap = new QAction(tr("Snap to Grid"));
        snap->setCheckable(true);
        snap->setChecked(true);
        settingsMenu->addAction(snap);
        connect(snap, &QAction::changed, this, &MainWindow::updateSnapToGrid);
        //Autolayout
        QAction *filter = new QAction(tr("Autolayout"));
        settingsMenu->addAction(filter);
        connect(filter, &QAction::triggered, this, &MainWindow::autoLayout);
    }

    QFrame *bottomFrame = new QFrame();
    bottomFrame->setMinimumHeight(20);
    layout->addWidget(bottomFrame);
}

void MainWindow::filterPressed()
{
    QAction *action = static_cast<QAction*>(sender());

    QPointF center = view->mapToScene(view->viewport()->rect().center());
    center.setX(center.x() - 100);
    center.setY(center.y() - 65);

    if (action->text() == "Filter 1")
        scene->addNode(center, action->text(), 1, 1);
    if (action->text() == "Filter 2")
        scene->addNode(center, action->text(), 2, 3);
    if (action->text() == "Filter 3")
        scene->addNode(center, action->text(), 4, 3);
}

void MainWindow::autoLayout()
{

}

void MainWindow::updateGridPic()
{
    if(gridPic)
    {
        gridPic = false;
        scene->setBackgroundBrush(QColor(77, 77, 77));
    }
    else
    {
        gridPic = true;
        scene->setBackgroundBrush(grid);
    }
}
void MainWindow::updateSnapToGrid()
{
    scene->toggleSnap();
    scene->snapToGrid(false);
}

void MainWindow::onActionQuitTriggered()
{
    close();
}
