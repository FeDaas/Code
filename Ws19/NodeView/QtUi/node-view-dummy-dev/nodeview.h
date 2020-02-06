#ifndef NODEVIEW_H
#define NODEVIEW_H

#include <QGraphicsView>
#include <QTimer>
#include <set>


class NodeScene;


class NodeView : public QGraphicsView
{
Q_OBJECT

public:
    NodeView(NodeScene *scene, QWidget *parent = nullptr);
    NodeView(QWidget *parent = nullptr);
    ~NodeView() override;

    void zoomIn();
    void zoomOut();

public slots:
    void autoScroll();

    void mousePressEvent(QMouseEvent *event) override;
    void mouseReleaseEvent(QMouseEvent *event) override;
    void mouseMoveEvent(QMouseEvent *event) override;

    void keyPressEvent(QKeyEvent *event) override;
    void keyReleaseEvent(QKeyEvent *event) override;

private:
    QTimer *timer;
    QPointF mousePos;

    std::set<int> pressedKeys;
};

#endif // NODEVIEW_H
